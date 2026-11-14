# ✨ Igreja Luz e Esperança — Aplicativo Android

Aplicativo nativo para Android desenvolvido em Java para divulgar informações da Igreja Luz e Esperança. O projeto mantém a logo, as cores e as imagens do [site da igreja](https://github.com/alliferlopes/SiteIgreja), com navegação própria para celular.

O app apresenta o conteúdo institucional, a agenda de eventos, informações de contato e um formulário para envio de pedidos de oração. Os dados dinâmicos são obtidos pelo backend Flask do site.

---

# 🚀 Tecnologias Utilizadas

## ☕ Java

Linguagem usada para desenvolver as telas e funcionalidades nativas do aplicativo.

## 🤖 Android SDK

Plataforma para compilar e executar o aplicativo. O projeto usa compile SDK 35 e oferece suporte a partir do Android 6.0 (API 23).

## 🧰 Android Studio e Gradle

Ferramentas para abrir, compilar e instalar o app. O projeto usa Java 17 e Android Gradle Plugin 9.5.1.

## 🌐 Flask API

Backend do site que fornece os eventos e recebe os pedidos de oração.

## 🗄️ MySQL

Banco de dados usado pelo backend Flask. O aplicativo Android não acessa o MySQL diretamente.

---

# 📌 Funcionalidades do Aplicativo

## 🏠 Início

* Carrossel com as imagens do site.
* Apresentação da missão e da comunidade.
* Destaque dos próximos eventos.

## 📖 Sobre

* História e visão da igreja.
* Valores institucionais.
* Imagens originais do site.

## 📅 Agenda

* Lista de eventos carregados do backend.
* Exibição de datas e horários.

## 📞 Contato

* Endereço, telefone e e-mail.
* Mapa do Google incorporado.
* Atalhos para abrir o mapa e iniciar uma ligação ou e-mail.

## 🙏 Pedido de Oração

* Formulário com nome, e-mail e pedido.
* Envio do pedido para o backend Flask.

## 📱 Navegação para celular

* Barra de navegação fixa na parte inferior.
* Interface adaptada para telas de celulares e tablets.
* Identidade visual em preto, branco e dourado, com a logo e as imagens originais.

---

# 🛠 Como Abrir e Executar

## 1️⃣ Abrir no Android Studio

Abra a pasta `projeto-app` no Android Studio e aguarde a sincronização do Gradle.

## 2️⃣ Configurar o endereço do backend

O app usa por padrão `http://10.0.2.2:5000`. Se o endereço ou IP do servidor Flask mudar, atualize `BASE_URL` em:

```text
projeto-app/app/src/main/java/br/com/luzespe/app/data/ApiClient.java
```

O endereço `10.0.2.2` funciona no emulador do Android Studio e aponta para o computador que executa o Flask. Para testar em um celular físico, altere `BASE_URL` para o IPv4 do computador (por exemplo, `http://192.168.0.128:5000`); celular e computador devem estar na mesma rede, e o firewall precisa liberar a porta 5000.

## 3️⃣ Iniciar o backend

Configure e inicie o site Flask seguindo as instruções do [README do site](https://github.com/alliferlopes/SiteIgreja). O MySQL também precisa estar ligado para acessar eventos e salvar pedidos de oração.

## 4️⃣ Executar no emulador

No Android Studio, selecione um emulador e clique em **Run**.

## 5️⃣ Gerar o APK de depuração

Na raiz do repositório, execute:

```powershell
.\projeto-app\gradlew.bat -p .\projeto-app assembleDebug
```

O APK será gerado em:

```text
projeto-app/app/build/outputs/apk/debug/app-debug.apk
```

Copie o APK para o celular e abra-o para instalar. Se o Android solicitar, permita a instalação de aplicativos dessa fonte.

---

# 🔌 API do Aplicativo

As rotas públicas usadas pelo app são fornecidas pelo backend Flask do site:

| Método | Rota | Uso |
| --- | --- | --- |
| `GET` | `/api/eventos` | Lista eventos publicados |
| `POST` | `/api/oracoes` | Envia um pedido com nome, e-mail e mensagem |

O banco utilizado pelo backend é `igreja_db`, definido em `banco.sql` no projeto do site. As credenciais do banco permanecem no servidor Flask.

---

# 🖼️ Imagens e Identidade Visual

Os recursos visuais copiados do site estão em `projeto-app/app/src/main/res/drawable-nodpi/`:

* `logo.png`
* `carrosel01.png`, `carrosel02.png` e `carrosel03.png`
* `imagem01.png` e `imagem02.png`
* `instagram.png`, `facebook.png` e `youtube.png`

---

# 📂 Estrutura do Projeto

As pastas separam as responsabilidades do aplicativo, seguindo uma organização por áreas semelhante à do site, com nomes apropriados para Android:

```text
AppIgreja/
├── projeto-app/
│   ├── app/
│   │   └── src/main/
│   │       ├── java/br/com/luzespe/app/
│   │       │   ├── data/ApiClient.java
│   │       │   └── ui/MainActivity.java
│   │       ├── res/
│   │       └── AndroidManifest.xml
│   ├── backend_api/
│   ├── gradle/
│   ├── gradlew
│   ├── build.gradle
│   └── settings.gradle
└── README.md
```

---

# 🔒 Observações para Publicação

* Para distribuir o app fora da rede local, hospede o backend em um servidor acessível pela internet e configure uma URL HTTPS em `ApiClient.java`.
* Não publique credenciais do banco de dados nem chaves secretas do Flask no aplicativo Android.
* A área administrativa não é exibida na navegação do aplicativo. O gerenciamento administrativo permanece disponível no site.

---

# 👨‍💻 Autor

Projeto desenvolvido por Allifer para fins acadêmicos, estudos e portfólio.
