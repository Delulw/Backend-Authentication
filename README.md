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

Los keystores, certificados y archivos de entorno están excluidos de Git. HTTPS usa
un keystore PKCS12 local y solo habilita TLS 1.2 y TLS 1.3.

## Seguridad base y TLS

La configuración stateless permite `/api/auth/**`, requiere autenticación para las
demás rutas y exige el rol `ADMIN` en `/api/admin/**`. El servidor escucha HTTPS en el
puerto `8443` por defecto; no habilita un listener HTTP sin cifrar.

Para crear un certificado local, abre PowerShell en la raíz del proyecto y define una
contraseña local segura para el keystore:

```powershell
$env:TLS_KEYSTORE_PASSWORD = Read-Host "Contraseña local del keystore"
$env:TLS_KEYSTORE_PATH = "file:./local-dev.p12"
keytool -genkeypair -alias backend-local -keyalg RSA -keysize 2048 -validity 365 `
  -storetype PKCS12 -keystore local-dev.p12 `
  -storepass $env:TLS_KEYSTORE_PASSWORD -keypass $env:TLS_KEYSTORE_PASSWORD `
  -dname "CN=localhost" -ext "SAN=dns:localhost,ip:127.0.0.1"
mvn spring-boot:run
```

La ruta y la contraseña también se pueden configurar con las variables `TLS_KEYSTORE_PATH`
y `TLS_KEYSTORE_PASSWORD`. Mantén la contraseña en el entorno local; no la guardes en Git.
El archivo `local-dev.p12` está excluido del repositorio.

Comprueba el handshake local (el certificado autofirmado requiere `-k` solo para esta
prueba) y que una conexión HTTP sin cifrar al puerto HTTPS sea rechazada:

```powershell
curl.exe -k --tlsv1.2 --tls-max 1.2 https://localhost:8443/
curl.exe -k --tlsv1.3 --tls-max 1.3 https://localhost:8443/
curl.exe -v http://localhost:8443/
```
