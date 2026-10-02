# API REST

La API está disponible bajo `/api` cuando la aplicación está ejecutándose en `localhost:8080`.

## Vehículos

### Listar

```http
GET /api/vehiculos
```

### Buscar por matrícula

```http
GET /api/vehiculos/buscar?matricula=ABC-123
```

### Registrar

```http
POST /api/vehiculos
Content-Type: application/json
```

```json
{
  "matricula": "ABC-123",
  "marca": "Toyota",
  "modelo": "Hilux"
}
```

### Editar

```http
PUT /api/vehiculos/1
Content-Type: application/json
```

```json
{
  "matricula": "ABC-123",
  "marca": "Toyota",
  "modelo": "Hilux"
}
```

### Cambiar estado

```http
PUT /api/vehiculos/1/estado
Content-Type: application/json
```

```json
{
  "activo": false
}
```

## Empleados

### Listar

```http
GET /api/empleados
```

### Buscar por QR

```http
GET /api/empleados/qr?codigoQR=QR-CARLOS-001
```

### Registrar

```http
POST /api/empleados
Content-Type: application/json
```

```json
{
  "nombre": "Carlos Perez",
  "codigoQR": "QR-CARLOS-001"
}
```

### Editar

```http
PUT /api/empleados/1
Content-Type: application/json
```

```json
{
  "nombre": "Carlos Perez",
  "codigoQR": "QR-CARLOS-001"
}
```

### Cambiar estado

```http
PUT /api/empleados/1/estado
Content-Type: application/json
```

```json
{
  "activo": false
}
```

## Asignaciones

### Listar

```http
GET /api/asignaciones
```

### Crear

```http
POST /api/asignaciones
Content-Type: application/json
```

```json
{
  "idVehiculo": 1,
  "idEmpleado": 1
}
```

Una asignación requiere que el vehículo y el empleado existan, estén activos y no tengan otra asignación activa.

### Finalizar

```http
PUT /api/asignaciones/1/finalizar
```

## Viajes

### Listar

```http
GET /api/viajes
```

Los viajes se crean cuando una detección de cámara procesa una salida autorizada.

## Cámara

### Registrar detección

```http
POST /api/camara
Content-Type: application/json
```

```json
{
  "matricula": "ABC-123"
}
```

La primera detección de una matrícula con asignación activa registra una salida. Si existe un viaje activo para el vehículo, la siguiente detección registra la entrada, finaliza el viaje y finaliza la asignación activa.

### Historial

```http
GET /api/eventos-camara
```

## Códigos HTTP principales

- `200 OK`: operación realizada correctamente.
- `201 Created`: recurso creado correctamente.
- `400 Bad Request`: datos de entrada inválidos o faltantes.
- `404 Not Found`: recurso inexistente.
- `409 Conflict`: operación incompatible con el estado actual, por ejemplo una matrícula/QR duplicado o una asignación que no puede crearse.

## Módulos avanzados

La API avanzada está bajo `/api/advanced` y requiere autenticación.

- `/keys` — inventario y entrega/devolución de llaves.
- `/inspections` — inspecciones y daños con referencias fotográficas.
- `/geofences` — geocercas y evaluación de posición.
- `/rules` — reglas operativas.
- `/schedules` y `/holidays` — horarios y feriados.
- `/approvals` — flujos de aprobación.
- `/notifications` — notificaciones internas.
- `/photos` — evidencias fotográficas.
- `/devices` — registro/heartbeat de hardware.
- `/modules` — activación/configuración de módulos.
- `/subscriptions` — planes por empresa.
- `/conductores` — perfil/licencia del conductor.
- `/rutas/{vehiculoId}` — rutas GPS.
- `/api-keys` — claves de integración administrativas.
- `/telemetry/{vehiculoId}` — telemetría.
- `/qr/temporary` y `/qr/consume` — tokens QR de un solo uso.
- `/assistant` — consultas operativas de solo lectura.

Los adaptadores de cámara/LPR y hardware aceptan eventos por API; la conexión física a un fabricante concreto sigue siendo una integración independiente.
