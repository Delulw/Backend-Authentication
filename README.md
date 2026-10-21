# Backend-Authentication
Práctica de backend authentication

## Estructura base

Proyecto Maven con Spring Boot y Java 17. La estructura inicial separa autenticación,
seguridad, usuarios y componentes comunes; las carpetas con `.gitkeep` quedan listas
para agregar su implementación en las siguientes etapas.

```text
src/
├── main/
│   ├── java/com/delulw/backendauthentication/
│   │   ├── auth/{controller,dto,service}/
│   │   ├── common/{exception,response}/
│   │   ├── config/
│   │   ├── security/
│   │   └── user/{model,repository,service}/
│   └── resources/
│       ├── application.yml
│       └── application-local.yml
└── test/
    └── java/com/delulw/backendauthentication/{auth,config,security}/
```

Los keystores, certificados y archivos de entorno están excluidos de Git; no se
incluyen secretos ni se activa TLS hasta configurar el certificado en una etapa posterior.
