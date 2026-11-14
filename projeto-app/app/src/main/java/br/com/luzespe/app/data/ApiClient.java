package br.com.luzespe.app.data;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Small REST client. Change BASE_URL for the Flask server used by your installation. */
public final class ApiClient {
    public static final String BASE_URL = "http://10.0.2.2:5000";
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static String token;

    public interface Callback { void onResult(int status, JSONObject body, String error); }
    public interface ListCallback { void onResult(int status, JSONArray body, String error); }

    private ApiClient() {}
    public static void setToken(String value) { token = value; }

    public static void getList(String path, ListCallback callback) {
        request("GET", path, null, (status, body, error) -> {
            JSONArray result = body == null ? null : body.optJSONArray("items");
            if (result == null && body != null) result = body.optJSONArray("eventos");
            if (result == null && body != null) result = body.optJSONArray("pedidos");
            if (result == null && body != null && body.optJSONArray("data") != null) result = body.optJSONArray("data");
            callback.onResult(status, result, error);
        });
    }

    public static void request(String method, String path, JSONObject data, Callback callback) {
        EXECUTOR.execute(() -> {
            HttpURLConnection connection = null;
            int status = 0;
            JSONObject response = null;
            String error = null;
            try {
                connection = (HttpURLConnection) new java.net.URI(BASE_URL + path).toURL().openConnection();
                connection.setRequestMethod(method);
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);
                connection.setRequestProperty("Accept", "application/json");
                if (token != null) connection.setRequestProperty("Authorization", "Bearer " + token);
                if (data != null) {
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    try (OutputStream output = connection.getOutputStream()) {
                        output.write(data.toString().getBytes(StandardCharsets.UTF_8));
                    }
                }
                status = connection.getResponseCode();
                InputStream stream = status < 400 ? connection.getInputStream() : connection.getErrorStream();
                if (stream != null) {
                    StringBuilder text = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) text.append(line);
                    }
                    if (text.length() > 0) {
                        try {
                            response = new JSONObject(text.toString());
                        } catch (org.json.JSONException notAnObject) {
                            JSONArray array = new JSONArray(text.toString());
                            response = new JSONObject().put("items", array);
                        }
                    }
                }
                if (status < 200 || status >= 300) error = response == null ? "Falha na comunicação com o servidor." : response.optString("message", response.optString("erro", "Falha na comunicação com o servidor."));
            } catch (Exception exception) {
                error = "Não foi possível conectar ao servidor. Confira a URL da API e sua conexão.";
            } finally {
                if (connection != null) connection.disconnect();
            }
            final int resultStatus = status;
            final JSONObject resultBody = response;
            final String resultError = error;
            MAIN.post(() -> callback.onResult(resultStatus, resultBody, resultError));
        });
    }
}
