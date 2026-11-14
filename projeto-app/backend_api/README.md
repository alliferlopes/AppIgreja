# Rotas REST para o app Android

Este diretório guarda uma cópia de referência do blueprint instalado em `C:\xampp\htdocs\SiteIgreja\projeto-igreja\routes\api_routes.py`.

As rotas retornam JSON em `items`, validam a senha armazenada em hash na tabela `usuarios` e assinam tokens Bearer com expiração de 12 horas. Eventos são validados e serializados usando as colunas `data` e `hora` do `banco.sql`.

Para produção, defina `FLASK_SECRET_KEY` e `FLASK_API_TOKEN_SECRET` como segredos aleatórios antes de iniciar o servidor. Os valores padrão do site atual são apenas para desenvolvimento local.
