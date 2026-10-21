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

## Seguridad base y JWT

La configuración stateless permite `/api/auth/**`, requiere un Bearer JWT para las
demás rutas y exige el rol `ADMIN` en `/api/admin/**`. El endpoint
`POST /api/auth/login` valida las credenciales configuradas y devuelve un JWT firmado
con HS256. Las respuestas `401` y `403` usan un cuerpo JSON común.

Para desarrollo, la autenticación usa una cuenta en memoria configurada por variables
de entorno (no una base de datos). Antes de iniciar la aplicación, define:

```powershell
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:JWT_SECRET_BASE64 = [Convert]::ToBase64String($bytes)
$rng.Dispose()
$env:JWT_EXPIRATION_MS = "900000"
$env:AUTH_USERNAME = "<usuario>"
$env:AUTH_PASSWORD = "<contraseña>"
$env:AUTH_ROLES = "USER"
mvn spring-boot:run
```

No guardes valores reales de esas variables en el repositorio. Se pueden configurar
varios roles separados por comas, por ejemplo `USER,ADMIN`. TLS y los certificados se
incorporarán en una etapa separada.
