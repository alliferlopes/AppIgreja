package br.com.luzespe.app.ui;

import br.com.luzespe.app.R;
import br.com.luzespe.app.data.ApiClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.webkit.WebSettings;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Native Android interface following the visual identity and content of SiteIgreja. */
public class MainActivity extends Activity {
    private static final int BLACK = Color.rgb(17, 17, 17);
    private static final int PAPER = Color.rgb(248, 249, 250);
    private static final int WHITE = Color.WHITE;
    private static final int TEXT = Color.rgb(33, 37, 41);
    private static final int MUTED = Color.rgb(108, 117, 125);
    private static final int GOLD = Color.rgb(255, 183, 0);
    private static final String CHURCH_NAME = "Igreja Luz e Esperança";
    private static final String EMAIL = "contato@igrejaluzesperanca.com.br";
    private static final String PHONE = "(11) 99999-9999";
    private static final String ADDRESS = "Centro, Guarulhos - SP";
    private static final String[] NAV_ITEMS = {"Home", "Sobre", "Agenda", "Contato", "Oração"};
    private static final String[] NAV_LABELS = {"Início", "Sobre", "Agenda", "Contato", "Oração"};
    private static final int[] NAV_ICONS = {
            R.drawable.ic_bi_house_door,
            R.drawable.ic_bi_info_circle,
            R.drawable.ic_bi_calendar_event,
            R.drawable.ic_bi_geo_alt,
            R.drawable.ic_bi_heart
    };

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final List<JSONObject> events = new ArrayList<>();
    private final List<TextView> carouselDots = new ArrayList<>();
    private LinearLayout root;
    private LinearLayout content;
    private LinearLayout eventListContainer;
    private LinearLayout adminEventContainer;
    private LinearLayout adminPrayerContainer;
    private ViewFlipper carousel;
    private Runnable carouselAdvance;
    private String currentPage = "Home";
    private String adminToken;
    private JSONObject editingEvent;
    private float swipeStartX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showPage("Home");
    }

    @Override
    protected void onDestroy() {
        stopCarousel();
        super.onDestroy();
    }

    private void showPage(String page) {
        stopCarousel();
        currentPage = page;
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BLACK);
        if (Build.VERSION.SDK_INT >= 35) {
            root.setOnApplyWindowInsetsListener((view, windowInsets) -> {
                android.graphics.Insets bars = windowInsets.getInsets(WindowInsets.Type.systemBars());
                view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return windowInsets;
            });
        }
        setContentView(root);

        addHeader();

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setClipToPadding(false);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(PAPER);
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        addNavigation();

        switch (page) {
            case "Home": homePage(); break;
            case "Sobre": aboutPage(); break;
            case "Agenda": agendaPage(); break;
            case "Contato": contactPage(); break;
            case "Oração": prayerPage(); break;
            case "Admin": adminLoginPage(); break;
            case "Painel": adminDashboardPage(); break;
            case "Novo evento": eventFormPage(null); break;
            case "Editar evento": eventFormPage(editingEvent); break;
            default: homePage();
        }

        addFooter();
    }

    private void addHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(7), dp(16), dp(7));
        header.setBackgroundColor(BLACK);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.logo);
        logo.setContentDescription("Logo da Igreja Luz e Esperança");
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logo.setOnClickListener(view -> showPage("Home"));
        header.addView(logo, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout identity = new LinearLayout(this);
        identity.setOrientation(LinearLayout.VERTICAL);
        identity.setPadding(dp(12), 0, 0, 0);
        TextView title = text(CHURCH_NAME, 18, WHITE, true);
        TextView tagline = text("Fé • Esperança • Amor", 11, Color.rgb(220, 220, 220), false);
        identity.addView(title);
        identity.addView(tagline, margin(-1, -2, 0, 1, 0, 0));
        header.addView(identity, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(header);
    }

    private void addNavigation() {
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setGravity(Gravity.CENTER_VERTICAL);
        tabs.setPadding(dp(5), dp(7), dp(5), dp(7));
        tabs.setBackgroundColor(WHITE);
        tabs.setElevation(dp(10));
        String active = navSelection(currentPage);
        for (int i = 0; i < NAV_ITEMS.length; i++) {
            String item = NAV_ITEMS[i];
            boolean selected = active.equals(item);
            LinearLayout tab = new LinearLayout(this);
            tab.setOrientation(LinearLayout.VERTICAL);
            tab.setGravity(Gravity.CENTER);
            tab.setPadding(0, dp(5), 0, dp(5));
            ImageView icon = new ImageView(this);
            icon.setImageResource(NAV_ICONS[i]);
            icon.setColorFilter(selected ? GOLD : MUTED);
            icon.setContentDescription(NAV_LABELS[i]);
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(22), dp(22));
            iconParams.bottomMargin = dp(2);
            tab.addView(icon, iconParams);
            TextView label = text(NAV_LABELS[i], 10, selected ? TEXT : MUTED, selected);
            label.setGravity(Gravity.CENTER);
            tab.addView(label);
            if (selected) tab.setBackground(round(0xFFFFF7DF, 18));
            tab.setOnClickListener(view -> showPage(item));
            tabs.addView(tab, new LinearLayout.LayoutParams(0, -2, 1));
        }
        root.addView(tabs, new LinearLayout.LayoutParams(-1, -2));
    }

    private String navSelection(String page) {
        if ("Painel".equals(page) || "Admin".equals(page) || "Novo evento".equals(page) || "Editar evento".equals(page)) return "Home";
        return page;
    }

    private void homePage() {
        addCarousel();

        LinearLayout mission = section();
        sectionHeading(mission, "Missão", "Compartilhando o amor de Deus através da fé cristã.");
        addParagraph(mission, "Nossa missão é levar a mensagem de amor, esperança e salvação através do evangelho de Jesus Cristo, alcançando pessoas de todas as idades e fortalecendo famílias por meio da fé cristã.");
        addParagraph(mission, "Buscamos ser uma igreja acolhedora, comprometida com os ensinamentos bíblicos e com o desenvolvimento espiritual, emocional e social da comunidade.");
        addParagraph(mission, "Também temos como propósito incentivar a solidariedade, o cuidado com o próximo e a prática dos valores cristãos no dia a dia.");
        content.addView(mission);

        LinearLayout community = section();
        community.setBackgroundColor(Color.rgb(241, 243, 245));
        sectionHeading(community, "Nossa Comunidade", null);
        addParagraph(community, "Nossa comunidade é formada por pessoas e famílias que compartilham da fé cristã e do desejo de crescer espiritualmente em união e amor ao próximo.");
        addParagraph(community, "Através dos cultos, encontros, eventos e ações sociais, incentivamos a comunhão entre os membros e o fortalecimento dos laços familiares e comunitários.");
        content.addView(community);

        LinearLayout upcoming = section();
        sectionHeading(upcoming, "Próximos Eventos", "Participe dos nossos próximos encontros.");
        content.addView(upcoming);
        eventListContainer = new LinearLayout(this);
        eventListContainer.setOrientation(LinearLayout.VERTICAL);
        content.addView(eventListContainer);
        loadEvents(false);
        addFooterLinkButton(content, "Ver agenda completa", () -> showPage("Agenda"));
    }

    private void addCarousel() {
        FrameLayout banner = new FrameLayout(this);
        banner.setBackgroundColor(BLACK);
        carousel = new ViewFlipper(this);
        carousel.setInAnimation(this, android.R.anim.fade_in);
        carousel.setOutAnimation(this, android.R.anim.fade_out);
        carousel.setFlipInterval(5000);

        int[] images = {R.drawable.carrosel01, R.drawable.carrosel02, R.drawable.carrosel03};
        String[] titles = {"Bem-vindo à Igreja Luz e Esperança", "Uma Comunidade de Fé", "Cultos e Eventos"};
        String[] subtitles = {"Levando fé, esperança e amor para toda comunidade.", "Caminhando juntos em amor e esperança.", "Confira a agenda e participe conosco."};
        for (int i = 0; i < images.length; i++) {
            FrameLayout slide = new FrameLayout(this);
            ImageView photo = new ImageView(this);
            photo.setImageResource(images[i]);
            photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
            slide.addView(photo, new FrameLayout.LayoutParams(-1, -1));

            View shade = new View(this);
            shade.setBackgroundColor(0x59000000);
            slide.addView(shade, new FrameLayout.LayoutParams(-1, -1));

            LinearLayout caption = new LinearLayout(this);
            caption.setOrientation(LinearLayout.VERTICAL);
            caption.setGravity(Gravity.BOTTOM | Gravity.START);
            caption.setPadding(dp(24), dp(14), dp(24), dp(45));
            FrameLayout.LayoutParams captionParams = new FrameLayout.LayoutParams(-1, -1, Gravity.BOTTOM | Gravity.START);
            slide.addView(caption, captionParams);
            TextView heading = text(titles[i], 23, WHITE, true);
            caption.addView(heading);
            TextView subheading = text(subtitles[i], 14, WHITE, false);
            subheading.setPadding(0, dp(7), 0, 0);
            caption.addView(subheading);
            carousel.addView(slide, new FrameLayout.LayoutParams(-1, -1));
        }
        banner.addView(carousel, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout indicators = new LinearLayout(this);
        indicators.setGravity(Gravity.CENTER);
        carouselDots.clear();
        for (int i = 0; i < images.length; i++) {
            TextView dot = text("●", 11, i == 0 ? WHITE : 0x99FFFFFF, true);
            dot.setPadding(dp(6), dp(4), dp(6), dp(9));
            final int index = i;
            dot.setOnClickListener(view -> {
                carousel.setDisplayedChild(index);
                updateCarouselDots();
            });
            carouselDots.add(dot);
            indicators.addView(dot);
        }
        FrameLayout.LayoutParams indicatorsParams = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        banner.addView(indicators, indicatorsParams);
        banner.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                swipeStartX = event.getX();
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                float difference = event.getX() - swipeStartX;
                if (Math.abs(difference) > dp(40)) {
                    if (difference < 0) carousel.showNext(); else carousel.showPrevious();
                    updateCarouselDots();
                }
                return true;
            }
            return true;
        });

        content.addView(banner, new LinearLayout.LayoutParams(-1, dp(270)));
        carouselAdvance = new Runnable() {
            @Override public void run() {
                if ("Home".equals(currentPage) && carousel != null) {
                    carousel.showNext();
                    updateCarouselDots();
                    mainHandler.postDelayed(this, 5000);
                }
            }
        };
        mainHandler.postDelayed(carouselAdvance, 5000);
    }

    private void stopCarousel() {
        if (carouselAdvance != null) mainHandler.removeCallbacks(carouselAdvance);
        carouselAdvance = null;
        carousel = null;
    }

    private void updateCarouselDots() {
        if (carousel == null) return;
        int active = carousel.getDisplayedChild();
        for (int i = 0; i < carouselDots.size(); i++) carouselDots.get(i).setTextColor(i == active ? WHITE : 0x99FFFFFF);
    }

    private void aboutPage() {
        LinearLayout intro = section();
        sectionHeading(intro, "Sobre Nós", "Conheça a história, visão e valores da Igreja Luz e Esperança.");
        content.addView(intro);

        addImageStory(R.drawable.imagem01_transparente, "História da Igreja",
                "A Igreja Luz e Esperança nasceu com o propósito de compartilhar o amor de Deus e transformar vidas através da fé cristã. Desde o início de sua trajetória, a igreja tem trabalhado para acolher pessoas, fortalecer famílias e levar esperança à comunidade por meio da Palavra de Deus, do discipulado e das ações sociais.\n\n" +
                "Ao longo dos anos, a igreja cresceu e passou a desenvolver diversos projetos espirituais e comunitários voltados ao apoio de crianças, jovens, adultos e idosos, sempre buscando promover união, solidariedade e crescimento espiritual.\n\n" +
                "Nossa história é marcada pela dedicação, perseverança e compromisso em servir à comunidade com amor, humildade e responsabilidade.");
        addImageStory(R.drawable.imagem02, "Visão",
                "Nossa visão é ser uma igreja reconhecida pelo compromisso com a Palavra de Deus, pelo amor ao próximo e pela dedicação em transformar vidas através da fé cristã.\n\n" +
                "Desejamos alcançar pessoas de diferentes realidades, levando esperança e mostrando que, através de Deus, é possível encontrar restauração, paz e direção para a vida.\n\n" +
                "Também buscamos ampliar projetos sociais, ações comunitárias e iniciativas que contribuam para o bem-estar espiritual e social da população.");

        LinearLayout values = section();
        sectionHeading(values, "Valores", null);
        LinearLayout valuesCard = new LinearLayout(this);
        valuesCard.setOrientation(LinearLayout.VERTICAL);
        valuesCard.setPadding(dp(18), dp(12), dp(18), dp(12));
        valuesCard.setBackground(round(WHITE, 12));
        valuesCard.setElevation(dp(2));
        String[] churchValues = {
                "Amor ao próximo",
                "Fé e confiança em Deus",
                "Respeito e união entre as pessoas",
                "Humildade e solidariedade",
                "Compromisso com a Palavra de Deus",
                "Responsabilidade social",
                "Acolhimento às famílias",
                "Honestidade e integridade"
        };
        for (int i = 0; i < churchValues.length; i++) {
            TextView value = text("•  " + churchValues[i], 15, MUTED, false);
            value.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
            value.setPadding(0, dp(9), 0, dp(9));
            valuesCard.addView(value, new LinearLayout.LayoutParams(-1, -2));
            if (i < churchValues.length - 1) {
                View divider = new View(this);
                divider.setBackgroundColor(0xFFE9E9E9);
                valuesCard.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));
            }
        }
        values.addView(valuesCard, margin(-1, -2, 0, 8, 0, 8));
        content.addView(values);
    }

    private void addImageStory(int imageResource, String title, String body) {
        LinearLayout section = section();
        ImageView image = new ImageView(this);
        image.setImageResource(imageResource);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setContentDescription(title);
        image.setBackgroundColor(Color.TRANSPARENT);
        section.addView(image, margin(-1, dp(190), 0, 0, 0, 16));
        add(section, title, 23, TEXT, true);
        addParagraph(section, body);
        content.addView(section);
    }

    private void agendaPage() {
        LinearLayout section = section();
        sectionHeading(section, "Agenda de Eventos", "Confira os próximos eventos e participe conosco.");
        content.addView(section);
        eventListContainer = new LinearLayout(this);
        eventListContainer.setOrientation(LinearLayout.VERTICAL);
        content.addView(eventListContainer);
        loadEvents(true);
        addFooterLinkButton(content, "Atualizar agenda", () -> showPage("Agenda"));
    }

    private void loadEvents(boolean fullList) {
        ApiClient.getList("/api/eventos", (status, result, error) -> {
            if (!"Home".equals(currentPage) && !"Agenda".equals(currentPage)) return;
            if (result == null) {
                showPublicEventMessage(fullList ? (error == null ? "Não há eventos disponíveis no momento." : error) : "Não foi possível carregar os próximos eventos.");
                return;
            }
            events.clear();
            for (int i = 0; i < result.length(); i++) {
                JSONObject event = result.optJSONObject(i);
                if (event != null) events.add(event);
            }
            int limit = fullList ? events.size() : Math.min(3, events.size());
            if (limit == 0) {
                showPublicEventMessage("Nenhum evento cadastrado no momento. Volte em breve para conferir a agenda.");
                return;
            }
            for (int i = 0; i < limit; i++) addEventCard(events.get(i));
        });
    }

    private void showPublicEventMessage(String message) {
        if (eventListContainer == null) return;
        LinearLayout holder = section();
        card(holder, "Próximos encontros", message);
        eventListContainer.addView(holder);
    }

    private void addEventCard(JSONObject event) {
        LinearLayout holder = section();
        String hour = event.optString("hora", event.optString("horario", ""));
        LinearLayout eventCard = new LinearLayout(this);
        eventCard.setOrientation(LinearLayout.VERTICAL);
        eventCard.setPadding(dp(18), dp(18), dp(18), dp(18));
        eventCard.setBackground(round(WHITE, 12));
        eventCard.setElevation(dp(2));
        add(eventCard, event.optString("titulo", "Evento"), 19, TEXT, true);
        addParagraph(eventCard, event.optString("descricao", ""));
        TextView when = text("▦   Data: " + event.optString("data", "") + "\n◷   Hora: " + hour, 14, TEXT, false);
        when.setPadding(0, dp(10), 0, 0);
        eventCard.addView(when);
        holder.addView(eventCard);
        if (eventListContainer != null) eventListContainer.addView(holder);
    }

    private void prayerPage() {
        LinearLayout outer = section();
        LinearLayout formCard = new LinearLayout(this);
        formCard.setOrientation(LinearLayout.VERTICAL);
        formCard.setPadding(dp(22), dp(24), dp(22), dp(24));
        formCard.setBackground(round(WHITE, 12));
        formCard.setElevation(dp(2));
        sectionHeading(formCard, "Pedido de Oração", "Envie seu pedido de oração. Estaremos orando por você e sua família.");

        EditText name = formInput(formCard, "Nome", "Digite seu nome", false);
        EditText email = formInput(formCard, "Email", "Digite seu email", false);
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText prayer = formInput(formCard, "Pedido", "Escreva seu pedido de oração", false);
        prayer.setSingleLine(false);
        prayer.setMinLines(5);
        prayer.setGravity(Gravity.TOP | Gravity.START);
        addPrimaryButton(formCard, "Enviar Pedido", () -> submitPrayer(name, email, prayer));
        outer.addView(formCard);
        content.addView(outer);
    }

    private void submitPrayer(EditText name, EditText email, EditText prayer) {
        String person = name.getText().toString().trim();
        String address = email.getText().toString().trim();
        String message = prayer.getText().toString().trim();
        if (person.isEmpty()) { name.setError("Informe seu nome."); return; }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(address).matches()) { email.setError("Informe um e-mail válido."); return; }
        if (message.length() < 5) { prayer.setError("Escreva seu pedido de oração."); return; }
        try {
            JSONObject payload = new JSONObject();
            payload.put("nome", person);
            payload.put("email", address);
            payload.put("pedido", message);
            ApiClient.request("POST", "/api/oracoes", payload, (status, body, error) -> {
                if (status >= 200 && status < 300) {
                    Toast.makeText(this, "Pedido enviado com sucesso!", Toast.LENGTH_LONG).show();
                    showPage("Home");
                } else toast(error == null ? "Não foi possível enviar o pedido." : error);
            });
        } catch (JSONException exception) {
            toast("Não foi possível preparar seu pedido.");
        }
    }

    private void contactPage() {
        LinearLayout intro = section();
        sectionHeading(intro, "Contato", "Entre em contato conosco. Será um prazer falar com você.");
        content.addView(intro);

        LinearLayout infoSection = section();
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(20), dp(20), dp(20), dp(20));
        info.setBackground(round(WHITE, 12));
        info.setElevation(dp(2));
        add(info, "Informações", 21, TEXT, true);
        addContactItem(info, R.drawable.ic_bi_geo_alt, "Endereço", ADDRESS, this::openMap);
        addContactItem(info, R.drawable.ic_bi_telephone, "Telefone", PHONE,
                () -> startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+5511999999999"))));
        addContactItem(info, R.drawable.ic_bi_envelope, "Email", EMAIL,
                () -> startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + EMAIL))));
        infoSection.addView(info);
        addPrimaryButton(infoSection, "Abrir no Google Maps", this::openMap);
        content.addView(infoSection);

        LinearLayout mapPreview = section();
        WebView map = new WebView(this);
        map.setWebViewClient(new WebViewClient());
        map.setWebChromeClient(new WebChromeClient());
        WebSettings settings = map.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        map.setBackground(round(WHITE, 12));
        String mapUrl = "https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d14639.30729396408!2d-46.540352232825356!3d-23.466710378798904!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x94cef55c7c86048d%3A0xb46307dc4ad8b735!2sCentro%2C%20Guarulhos%20-%20SP!5e0!3m2!1spt-BR!2sbr!4v1778425899184!5m2!1spt-BR!2sbr";
        String mapHtml = "<!doctype html><html lang='pt-BR'><head>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "</head><body style='margin:0;padding:0;overflow:hidden'>" +
                "<iframe src='" + mapUrl + "' width='100%' height='100%' " +
                "style='border:0;position:absolute;inset:0' allowfullscreen " +
                "loading='lazy' referrerpolicy='no-referrer-when-downgrade'></iframe>" +
                "</body></html>";
        map.loadDataWithBaseURL("https://www.google.com", mapHtml, "text/html", "UTF-8", null);
        mapPreview.addView(map, margin(-1, dp(260), 0, 0, 0, 12));
        addSecondaryButton(mapPreview, "Ver rota no Google Maps", this::openMap);
        content.addView(mapPreview);
    }

    private void addContactItem(LinearLayout parent, int iconResource, String title, String value, Runnable action) {
        LinearLayout headingRow = new LinearLayout(this);
        headingRow.setGravity(Gravity.CENTER_VERTICAL);
        headingRow.setPadding(0, dp(20), 0, dp(4));
        ImageView icon = new ImageView(this);
        icon.setImageResource(iconResource);
        icon.setColorFilter(GOLD);
        icon.setContentDescription(title);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        headingRow.addView(icon, new LinearLayout.LayoutParams(dp(20), dp(20)));
        TextView heading = text(title, 15, TEXT, true);
        heading.setPadding(dp(9), 0, 0, 0);
        headingRow.addView(heading);
        headingRow.setOnClickListener(view -> action.run());
        parent.addView(headingRow);
        TextView detail = text(value, 14, MUTED, false);
        parent.addView(detail);
        detail.setOnClickListener(view -> action.run());
    }

    private void openMap() {
        Uri location = Uri.parse("geo:-23.4667104,-46.5403522?q=Centro%2C%20Guarulhos%20-%20SP");
        Intent intent = new Intent(Intent.ACTION_VIEW, location);
        if (intent.resolveActivity(getPackageManager()) != null) startActivity(intent);
        else startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=Centro%2C+Guarulhos+-+SP")));
    }

    private void openAdmin() {
        if (adminToken != null) {
            ApiClient.setToken(adminToken);
            showPage("Painel");
        } else showPage("Admin");
    }

    private void adminLoginPage() {
        LinearLayout section = section();
        LinearLayout loginCard = new LinearLayout(this);
        loginCard.setOrientation(LinearLayout.VERTICAL);
        loginCard.setPadding(dp(22), dp(24), dp(22), dp(24));
        loginCard.setBackground(round(WHITE, 12));
        loginCard.setElevation(dp(2));
        sectionHeading(loginCard, "Área Administrativa", "Entre com sua conta de administrador.");
        EditText email = formInput(loginCard, "E-mail", "Digite seu e-mail", false);
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText password = formInput(loginCard, "Senha", "Digite sua senha", true);
        addPrimaryButton(loginCard, "Entrar", () -> login(email.getText().toString().trim(), password.getText().toString()));
        section.addView(loginCard);
        content.addView(section);
    }

    private void login(String email, String password) {
        if (email.isEmpty()) { toast("Informe seu e-mail."); return; }
        if (password.isEmpty()) { toast("Informe sua senha."); return; }
        try {
            JSONObject payload = new JSONObject();
            payload.put("email", email);
            payload.put("senha", password);
            ApiClient.request("POST", "/api/admin/login", payload, (status, body, error) -> {
                if (status >= 200 && status < 300 && body != null && body.has("token")) {
                    adminToken = body.optString("token");
                    ApiClient.setToken(adminToken);
                    showPage("Painel");
                } else toast(error == null ? "Credenciais inválidas ou serviço de login indisponível." : error);
            });
        } catch (JSONException exception) {
            toast("Não foi possível iniciar a sessão.");
        }
    }

    private void adminDashboardPage() {
        LinearLayout section = section();
        sectionHeading(section, "Painel Administrativo", "Gerencie eventos e acompanhe os pedidos de oração.");
        addPrimaryButton(section, "+  Cadastrar evento", () -> showPage("Novo evento"));
        content.addView(section);

        LinearLayout eventsSection = section();
        add(eventsSection, "Eventos cadastrados", 22, TEXT, true);
        content.addView(eventsSection);
        adminEventContainer = new LinearLayout(this);
        adminEventContainer.setOrientation(LinearLayout.VERTICAL);
        content.addView(adminEventContainer);
        ApiClient.getList("/api/admin/eventos", (status, list, error) -> {
            if (!"Painel".equals(currentPage)) return;
            if (list == null) {
                showAdminMessage(adminEventContainer, error == null ? "Não foi possível carregar os eventos." : error);
                return;
            }
            for (int i = 0; i < list.length(); i++) {
                JSONObject event = list.optJSONObject(i);
                if (event == null) continue;
                addAdminEvent(adminEventContainer, event);
            }
            if (list.length() == 0) showAdminMessage(adminEventContainer, "Nenhum evento cadastrado.");
        });

        LinearLayout prayersSection = section();
        add(prayersSection, "Pedidos de oração", 22, TEXT, true);
        content.addView(prayersSection);
        adminPrayerContainer = new LinearLayout(this);
        adminPrayerContainer.setOrientation(LinearLayout.VERTICAL);
        content.addView(adminPrayerContainer);
        ApiClient.getList("/api/admin/oracoes", (status, list, error) -> {
            if (!"Painel".equals(currentPage)) return;
            if (list == null) {
                showAdminMessage(adminPrayerContainer, error == null ? "Não foi possível carregar os pedidos." : error);
                return;
            }
            if (list.length() == 0) showAdminMessage(adminPrayerContainer, "Nenhum pedido recebido.");
            for (int i = 0; i < list.length(); i++) {
                JSONObject prayer = list.optJSONObject(i);
                if (prayer != null) {
                    LinearLayout item = section();
                    card(item, prayer.optString("nome", "Anônimo"), prayer.optString("pedido", ""));
                    adminPrayerContainer.addView(item);
                }
            }
        });

        LinearLayout signOutSection = section();
        addSecondaryButton(signOutSection, "Sair da área administrativa", () -> {
            adminToken = null;
            ApiClient.setToken(null);
            showPage("Home");
        });
        content.addView(signOutSection);
    }

    private void addAdminEvent(LinearLayout target, JSONObject event) {
        LinearLayout holder = section();
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(12));
        card.setBackground(round(WHITE, 12));
        card.setElevation(dp(2));
        add(card, event.optString("titulo", "Evento"), 18, TEXT, true);
        addParagraph(card, event.optString("data", "") + " • " + event.optString("hora", event.optString("horario", "")));
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        addSmallButton(actions, "Editar", () -> { editingEvent = event; showPage("Editar evento"); });
        addSmallButton(actions, "Excluir", () -> confirmDeleteEvent(event));
        card.addView(actions);
        holder.addView(card);
        target.addView(holder);
    }

    private void showAdminMessage(LinearLayout target, String message) {
        if (target == null) return;
        LinearLayout item = section();
        card(item, "", message);
        target.addView(item);
    }

    private void confirmDeleteEvent(JSONObject event) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir evento")
                .setMessage("Deseja remover “" + event.optString("titulo", "este evento") + "” da agenda?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> deleteEvent(event))
                .show();
    }

    private void deleteEvent(JSONObject event) {
        ApiClient.request("DELETE", "/api/admin/eventos/" + event.optString("id"), null, (status, body, error) -> {
            if (status >= 200 && status < 300) {
                toast("Evento removido.");
                showPage("Painel");
            } else toast(error == null ? "Não foi possível excluir o evento." : error);
        });
    }

    private void eventFormPage(JSONObject event) {
        boolean editing = event != null;
        LinearLayout section = section();
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(22), dp(24), dp(22), dp(24));
        form.setBackground(round(WHITE, 12));
        form.setElevation(dp(2));
        sectionHeading(form, editing ? "Editar evento" : "Novo evento", "Informe os dados que serão exibidos na agenda.");

        EditText title = formInput(form, "Título", "Nome do evento", false);
        EditText description = formInput(form, "Descrição", "Descrição do evento", false);
        description.setSingleLine(false);
        description.setMinLines(3);
        description.setGravity(Gravity.TOP | Gravity.START);
        EditText date = formInput(form, "Data", "AAAA-MM-DD", false);
        EditText hour = formInput(form, "Hora", "HH:MM", false);
        if (editing) {
            title.setText(event.optString("titulo"));
            description.setText(event.optString("descricao"));
            date.setText(event.optString("data"));
            hour.setText(event.optString("hora", event.optString("horario", "")));
        }
        addPrimaryButton(form, editing ? "Salvar alterações" : "Cadastrar evento", () -> saveEvent(event, title, description, date, hour));
        addSecondaryButton(form, "Cancelar", () -> showPage("Painel"));
        section.addView(form);
        content.addView(section);
    }

    private void saveEvent(JSONObject event, EditText title, EditText description, EditText date, EditText hour) {
        String name = title.getText().toString().trim();
        String eventDate = date.getText().toString().trim();
        if (name.isEmpty()) { title.setError("Informe o título."); return; }
        if (eventDate.isEmpty()) { date.setError("Informe a data."); return; }
        try {
            String time = hour.getText().toString().trim();
            JSONObject payload = new JSONObject();
            payload.put("titulo", name);
            payload.put("descricao", description.getText().toString().trim());
            payload.put("data", eventDate);
            payload.put("hora", time);
            payload.put("horario", time);
            String path = "/api/admin/eventos" + (event == null ? "" : "/" + event.optString("id"));
            ApiClient.request(event == null ? "POST" : "PUT", path, payload, (status, body, error) -> {
                if (status >= 200 && status < 300) {
                    toast("Evento salvo.");
                    showPage("Painel");
                } else toast(error == null ? "Não foi possível salvar o evento." : error);
            });
        } catch (JSONException exception) {
            toast("Não foi possível preparar o evento.");
        }
    }

    private void addFooter() {
        LinearLayout footer = new LinearLayout(this);
        footer.setOrientation(LinearLayout.VERTICAL);
        footer.setPadding(dp(22), dp(30), dp(22), dp(24));
        footer.setBackgroundColor(BLACK);
        add(footer, CHURCH_NAME, 21, WHITE, true);
        add(footer, "Levando fé, esperança e amor para toda comunidade.", 14, Color.rgb(220, 220, 220), false).setPadding(0, dp(8), 0, dp(18));

        add(footer, "Redes Sociais", 16, WHITE, true);
        LinearLayout social = new LinearLayout(this);
        social.setGravity(Gravity.CENTER_VERTICAL);
        social.setPadding(0, dp(10), 0, dp(20));
        addSocialIcon(social, R.drawable.instagram, "Instagram", "https://instagram.com");
        addSocialIcon(social, R.drawable.facebook, "Facebook", "https://facebook.com");
        addSocialIcon(social, R.drawable.youtube, "YouTube", "https://youtube.com");
        footer.addView(social);

        add(footer, "Contato", 16, WHITE, true);
        add(footer, EMAIL + "\n" + PHONE + "\n" + ADDRESS, 14, Color.rgb(220, 220, 220), false).setPadding(0, dp(8), 0, dp(18));
        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(68, 68, 68));
        footer.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));
        TextView copyright = text("© 2026 Igreja Luz e Esperança - Todos os direitos reservados.", 12, Color.rgb(205, 205, 205), false);
        copyright.setGravity(Gravity.CENTER);
        copyright.setPadding(0, dp(14), 0, 0);
        footer.addView(copyright);
        content.addView(footer);
    }

    private void addSocialIcon(LinearLayout parent, int image, String label, String url) {
        ImageView icon = new ImageView(this);
        icon.setImageResource(image);
        icon.setContentDescription(label);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(38), dp(38));
        params.setMargins(0, 0, dp(16), 0);
        parent.addView(icon, params);
    }

    private LinearLayout section() {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setPadding(dp(20), dp(25), dp(20), dp(22));
        return section;
    }

    private void sectionHeading(LinearLayout parent, String title, String subtitle) {
        TextView heading = text(title, 26, TEXT, true);
        heading.setGravity(Gravity.CENTER);
        parent.addView(heading, new LinearLayout.LayoutParams(-1, -2));
        if (subtitle != null && !subtitle.isEmpty()) {
            TextView subheading = text(subtitle, 15, MUTED, false);
            subheading.setGravity(Gravity.CENTER);
            subheading.setPadding(0, dp(8), 0, dp(18));
            parent.addView(subheading, new LinearLayout.LayoutParams(-1, -2));
        } else heading.setPadding(0, 0, 0, dp(15));
    }

    private void addParagraph(LinearLayout parent, String value) {
        TextView paragraph = text(value, 15, MUTED, false);
        paragraph.setLineSpacing(dp(3), 1.0f);
        paragraph.setPadding(0, dp(5), 0, dp(9));
        parent.addView(paragraph, new LinearLayout.LayoutParams(-1, -2));
    }

    private void card(LinearLayout parent, String title, String body) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackground(round(WHITE, 12));
        card.setElevation(dp(2));
        add(card, title, 18, TEXT, true);
        addParagraph(card, body);
        parent.addView(card, margin(-1, -2, 0, 8, 0, 8));
    }

    private EditText formInput(LinearLayout parent, String label, String hint, boolean password) {
        LinearLayout field = new LinearLayout(this);
        field.setOrientation(LinearLayout.VERTICAL);
        TextView labelView = text(label, 14, TEXT, true);
        labelView.setPadding(0, 0, 0, dp(5));
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setTextSize(16);
        input.setSingleLine(!"Pedido".equals(label) && !"Descrição".equals(label));
        input.setPadding(dp(14), dp(11), dp(14), dp(11));
        input.setBackground(round(Color.rgb(248, 249, 250), 8));
        if (password) input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        field.addView(labelView);
        field.addView(input, new LinearLayout.LayoutParams(-1, -2));
        parent.addView(field, margin(-1, -2, 0, 8, 0, 8));
        return input;
    }

    private void addPrimaryButton(LinearLayout parent, String label, Runnable action) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(WHITE);
        button.setPadding(dp(14), dp(8), dp(14), dp(8));
        button.setBackground(round(BLACK, 8));
        button.setOnClickListener(view -> action.run());
        parent.addView(button, margin(-1, dp(52), 0, 6, 0, 10));
    }

    private void addSecondaryButton(LinearLayout parent, String label, Runnable action) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setTextColor(BLACK);
        button.setBackground(round(Color.rgb(233, 236, 239), 8));
        button.setOnClickListener(view -> action.run());
        parent.addView(button, margin(-1, dp(48), 0, 5, 0, 10));
    }

    private void addFooterLinkButton(LinearLayout parent, String label, Runnable action) {
        LinearLayout holder = section();
        addPrimaryButton(holder, label, action);
        parent.addView(holder);
    }

    private void addSmallButton(LinearLayout parent, String label, Runnable action) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(13);
        button.setAllCaps(false);
        button.setTextColor(BLACK);
        button.setBackground(round(Color.rgb(240, 241, 242), 8));
        button.setOnClickListener(view -> action.run());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(46), 1);
        params.setMargins(dp(4), dp(8), dp(4), 0);
        parent.addView(button, params);
    }

    private TextView add(LinearLayout parent, String value, int size, int color, boolean bold) {
        TextView view = text(value, size, color, bold);
        parent.addView(view, new LinearLayout.LayoutParams(-1, -2));
        return view;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private GradientDrawable round(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private LinearLayout.LayoutParams margin(int width, int height, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
