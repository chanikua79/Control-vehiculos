# Arquitectura

## Capas

### `control.vehiculos`

Contiene las entidades y la lógica principal del dominio:

- `Vehiculo`
- `Empleado`
- `AsignacionVehiculo`
- `Viaje`
- `EventoCamara`
- `IdentificadorEmpleado`
- `IdentificadorQR`
- `GestorVehiculos`

`GestorVehiculos` coordina las operaciones del dominio y la persistencia mediante los DAO.

### `database`

Contiene la conexión SQLite, la creación de tablas y los objetos de acceso a datos:

- `Database`
- `InicializadorBD`
- `VehiculoDAO`
- `EmpleadoDAO`
- `AsignacionDAO`
- `ViajeDAO`
- `EventoCamaraDAO`

### `web`

Contiene la aplicación Spring Boot y el controlador REST.

### `static`

Contiene la interfaz web HTML/JavaScript servida directamente por Spring Boot.

## Persistencia

La aplicación utiliza SQLite mediante JDBC. La base local se llama `vehiculos.db` y se crea automáticamente cuando se inicializan las tablas.

Las tablas principales son:

- `vehiculos`
- `empleados`
- `asignaciones`
- `viajes`
- `eventos_camara`

Los cambios de vehículos, empleados, asignaciones y viajes relevantes se persisten durante las operaciones correspondientes.

## Flujo de cámara

```text
Detección de matrícula
        │
        ▼
POST /api/camara
        │
        ▼
GestorVehiculos.procesarEventoCamara()
        │
        ├── registra evento de cámara
        │
        ├── matrícula desconocida → termina
        │
        ├── viaje activo → entrada
        │       ├── actualiza viaje
        │       └── finaliza asignación
        │
        └── sin viaje activo → salida
                ├── verifica asignación activa
                └── crea viaje
```

La entrada actual de la cámara es una matrícula. La detección automática mediante visión por computador puede integrarse posteriormente sin cambiar el contrato principal del dominio.
