# Operación

## Arranque local

```bash
mvn clean verify
mvn spring-boot:run
```

Abrir `http://localhost:8080`.

## Acceso inicial

Usuario: `admin`

Contraseña inicial: `cambiar-me`, salvo que se defina `ADMIN_PASSWORD` antes del primer arranque.

Cambiar la contraseña inmediatamente desde la API o ampliar la interfaz según la política de la empresa.

## Docker

```bash
mvn clean package -DskipTests
docker compose up -d --build
```

La base de datos queda en el volumen `vehiculos_data`.

## API

Swagger: `/swagger-ui.html`

OpenAPI: `/v3/api-docs`

## Cámara

El sistema ya separa la detección de matrícula mediante `PlateRecognitionService`. La implementación incluida es deliberadamente neutra: no afirma reconocer matrículas sin un motor OCR/visión conectado.

Para integrar una cámara real se puede implementar:

```text
PlateRecognitionService
        ↓
OpenCV / OCR / modelo LPR
        ↓
PlateDetection
        ↓
/api/enterprise/camara/detecciones
        ↓
Gestión de entrada/salida
```
