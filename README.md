# Control Vehicular

Sistema web para control empresarial de vehículos, empleados, asignaciones, viajes y eventos de acceso. La arquitectura está preparada para integrar cámaras y reconocimiento automático de matrículas sin acoplar el núcleo del negocio a un proveedor concreto de visión.

## Funcionalidades

- REST API para vehículos, empleados, asignaciones, viajes y eventos de cámara.
- Persistencia SQLite.
- Dashboard operativo.
- Autenticación por sesión y contraseñas con BCrypt.
- Roles `ADMIN`, `SUPERVISOR` y `EMPLEADO`.
- Auditoría de acciones administrativas.
- Estados de flota: disponible, asignado/en circulación y mantenimiento mediante el módulo empresarial.
- Reservas de vehículos con detección de solapamientos.
- Mantenimiento y costos.
- Registro de combustible, kilometraje y porcentaje de combustible.
- Fuentes de cámara y detecciones de matrícula.
- Interfaz `PlateRecognitionService` preparada para OpenCV/OCR/LPR.
- Alertas automáticas de combustible bajo, mantenimiento próximo y eventos anómalos.
- Incidencias, documentación vehicular y checklists de salida.
- Control de acceso con QR/RFID/NFC/LPR/manual como métodos de identificación.
- Telemetría GPS y analítica por vehículo.
- Flujo automático de salida/entrada desde eventos LPR de cámara.
- Empresas, sedes y webhooks preparados para operación multi-sede.
- PWA instalable en móvil/escritorio y modo de contingencia de caché para recursos estáticos.
- Reportes CSV.
- Swagger/OpenAPI.
- Tests con Spring Boot Test.
- GitHub Actions para `mvn clean verify`.
- Docker y Docker Compose con healthcheck.
- Backups SQLite automáticos y backup manual para administradores.
- Monitorización Actuator (`/actuator/health`, métricas e información).
- Eventos en tiempo real mediante SSE.
- Base de datos configurable mediante `VEHICULOS_DB_PATH`.

## Arquitectura

```text
Frontend HTML/JS
       ↓
REST Controller
       ↓
Service
       ↓
DAO / SQLite
       ↓
Base de datos
```

El código legado `GestorVehiculos` mantiene la lógica funcional existente y el módulo `service` contiene las funciones empresariales nuevas. La siguiente evolución natural es migrar progresivamente toda la lógica antigua al mismo patrón Service/Repository.

## Requisitos

- Java 21
- Maven 3.9+
- SQLite incluido mediante Xerial JDBC

## Ejecutar

```bash
mvn clean verify
mvn spring-boot:run
```

Abrir:

`http://localhost:8080`

Acceso inicial: `admin` / `cambiar-me`, salvo que se haya definido `ADMIN_PASSWORD` antes del primer arranque. Cambiarla inmediatamente.

## Swagger

- `/swagger-ui.html`
- `/v3/api-docs`

## Endpoints empresariales

- `/api/auth/*`
- `/api/enterprise/dashboard`
- `/api/enterprise/vehiculos/*`
- `/api/enterprise/viajes`
- `/api/enterprise/reservas/*`
- `/api/enterprise/mantenimiento/*`
- `/api/enterprise/combustible`
- `/api/enterprise/camara/*`
- `/api/enterprise/alertas/*`
- `/api/enterprise/incidencias/*`
- `/api/enterprise/documentos/*`
- `/api/enterprise/checklists`
- `/api/enterprise/control-acceso`
- `/api/enterprise/gps/*`
- `/api/enterprise/analitica`
- `/api/enterprise/empresas` y `/api/enterprise/sedes`
- `/api/enterprise/webhooks`
- `/api/enterprise/auditoria`
- `/api/enterprise/usuarios/*`
- `/api/reportes/viajes.csv`

## Cámara y reconocimiento de matrículas

El sistema no incluye un modelo de reconocimiento de matrículas ficticio. La interfaz `PlateRecognitionService` permite conectar posteriormente OpenCV, OCR, un modelo LPR o un servicio externo. La API también admite detecciones manuales para probar el flujo completo antes de instalar hardware.

## CI

GitHub Actions ejecuta compilación y pruebas en Java 21 para pushes y pull requests a `main`.

## Docker

```bash
mvn clean package -DskipTests
docker compose up -d --build
```

La base de datos se conserva en el volumen `vehiculos_data`.

## Documentación adicional

- `docs/ARQUITECTURA.md`
- `docs/API.md`
- `docs/OPERACION.md`
- `CONTRIBUTING.md`
- `SECURITY.md`
- `CHANGELOG.md`

## Módulos avanzados incluidos

La versión actual incluye, además del núcleo de vehículos/viajes:

- centro avanzado de operación;
- llaves físicas y trazabilidad de entrega/devolución;
- inspecciones de daños y evidencias fotográficas;
- geocercas con evaluación de posición;
- reglas de flota, horarios y feriados;
- aprobaciones y notificaciones internas;
- dispositivos y heartbeat para cámaras/RFID/GPS;
- configuración modular;
- perfiles de conductores y licencias;
- rutas GPS y telemetría;
- QR temporales de un solo uso;
- administración de API keys;
- suscripciones por empresa;
- alertas automáticas para documentos, licencias y llaves;
- asistente operativo de solo lectura.

### Centro avanzado

Después de iniciar sesión: `http://localhost:8080/advanced.html`

### Validación

El entorno de construcción usado para preparar esta entrega no tenía Maven disponible. Se verificó la integridad del ZIP, balance estructural de los archivos Java y ejecución sintáctica de las sentencias de creación SQLite. En la máquina de desarrollo debe ejecutarse `mvn clean verify` antes del despliegue.
