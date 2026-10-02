package web.controller;

import control.vehiculos.AsignacionVehiculo;
import control.vehiculos.Empleado;
import control.vehiculos.EventoCamara;
import control.vehiculos.GestorVehiculos;
import control.vehiculos.Vehiculo;
import control.vehiculos.Viaje;
import database.EventoCamaraDAO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ControlVehiculosController {

    private final GestorVehiculos gestor;

    public ControlVehiculosController(GestorVehiculos gestor) {
        this.gestor = gestor;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("vehiculos", gestor.getVehiculos().size());
        resultado.put("empleados", gestor.getEmpleados().size());
        resultado.put("estado", "OPERATIVO");
        return resultado;
    }

    @GetMapping("/vehiculos")
    public List<Vehiculo> vehiculos() {
        return gestor.getVehiculos();
    }

    @GetMapping("/vehiculos/buscar")
    public ResponseEntity<?> buscarVehiculo(@RequestParam String matricula) {
        Vehiculo vehiculo = gestor.buscarVehiculoPorMatriculaPublica(matricula.trim());
        if (vehiculo == null) {
            return error(HttpStatus.NOT_FOUND, "Vehiculo no encontrado");
        }
        return ResponseEntity.ok(vehiculo);
    }

    @PostMapping("/vehiculos")
    public ResponseEntity<Map<String, Object>> registrarVehiculo(
            @RequestBody Map<String, String> datos) {

        String matricula = textoObligatorio(datos, "matricula");
        String marca = textoObligatorio(datos, "marca");
        String modelo = textoObligatorio(datos, "modelo");

        if (gestor.buscarVehiculoPorMatriculaPublica(matricula) != null) {
            return error(HttpStatus.CONFLICT,
                    "Ya existe un vehiculo con esa matricula");
        }

        Vehiculo vehiculo = new Vehiculo(
                gestor.obtenerSiguienteVehiculoId(),
                matricula,
                marca,
                modelo
        );

        gestor.agregarVehiculo(vehiculo);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Vehiculo registrado correctamente");
        respuesta.put("vehiculo", vehiculo);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PutMapping("/vehiculos/{id}")
    public ResponseEntity<Map<String, Object>> editarVehiculo(
            @PathVariable int id,
            @RequestBody Map<String, String> datos) {

        String matricula = textoObligatorio(datos, "matricula");
        String marca = textoObligatorio(datos, "marca");
        String modelo = textoObligatorio(datos, "modelo");

        if (!gestor.existeVehiculo(id)) {
            return error(HttpStatus.NOT_FOUND, "Vehiculo no encontrado");
        }

        if (gestor.existeOtraMatricula(matricula, id)) {
            return error(HttpStatus.CONFLICT,
                    "Ya existe otro vehiculo con esa matricula");
        }

        gestor.editarVehiculo(id, matricula, marca, modelo);
        return mensaje("Vehiculo actualizado correctamente");
    }

    @PutMapping("/vehiculos/{id}/estado")
    public ResponseEntity<Map<String, Object>> cambiarEstadoVehiculo(
            @PathVariable int id,
            @RequestBody Map<String, Boolean> datos) {

        Boolean activo = datos.get("activo");
        if (activo == null) {
            return error(HttpStatus.BAD_REQUEST,
                    "El campo activo es obligatorio");
        }

        if (!gestor.cambiarEstadoVehiculo(id, activo)) {
            return error(HttpStatus.NOT_FOUND, "Vehiculo no encontrado");
        }

        return mensaje(activo
                ? "Vehiculo activado correctamente"
                : "Vehiculo desactivado correctamente");
    }

    @GetMapping("/empleados")
    public List<Empleado> empleados() {
        return gestor.getEmpleados();
    }

    @GetMapping("/empleados/qr")
    public ResponseEntity<?> buscarEmpleadoPorQR(@RequestParam String codigoQR) {
        Empleado empleado = gestor.buscarEmpleadoPorQR(codigoQR.trim());
        if (empleado == null) {
            return error(HttpStatus.NOT_FOUND, "Empleado no encontrado");
        }
        return ResponseEntity.ok(empleado);
    }

    @PostMapping("/empleados")
    public ResponseEntity<Map<String, Object>> registrarEmpleado(
            @RequestBody Map<String, String> datos) {

        String nombre = textoObligatorio(datos, "nombre");
        String codigoQR = textoObligatorio(datos, "codigoQR");

        if (gestor.buscarEmpleadoPorQR(codigoQR) != null) {
            return error(HttpStatus.CONFLICT,
                    "Ya existe un empleado con ese codigo QR");
        }

        Empleado empleado = new Empleado(
                gestor.obtenerSiguienteEmpleadoId(),
                nombre,
                codigoQR
        );

        gestor.agregarEmpleado(empleado);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Empleado registrado correctamente");
        respuesta.put("empleado", empleado);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PutMapping("/empleados/{id}")
    public ResponseEntity<Map<String, Object>> editarEmpleado(
            @PathVariable int id,
            @RequestBody Map<String, String> datos) {

        String nombre = textoObligatorio(datos, "nombre");
        String codigoQR = textoObligatorio(datos, "codigoQR");

        if (!gestor.existeEmpleado(id)) {
            return error(HttpStatus.NOT_FOUND, "Empleado no encontrado");
        }

        if (gestor.existeOtroCodigoQR(codigoQR, id)) {
            return error(HttpStatus.CONFLICT,
                    "Ya existe otro empleado con ese codigo QR");
        }

        gestor.editarEmpleado(id, nombre, codigoQR);
        return mensaje("Empleado actualizado correctamente");
    }

    @PutMapping("/empleados/{id}/estado")
    public ResponseEntity<Map<String, Object>> cambiarEstadoEmpleado(
            @PathVariable int id,
            @RequestBody Map<String, Boolean> datos) {

        Boolean activo = datos.get("activo");
        if (activo == null) {
            return error(HttpStatus.BAD_REQUEST,
                    "El campo activo es obligatorio");
        }

        if (!gestor.cambiarEstadoEmpleado(id, activo)) {
            return error(HttpStatus.NOT_FOUND, "Empleado no encontrado");
        }

        return mensaje(activo
                ? "Empleado activado correctamente"
                : "Empleado desactivado correctamente");
    }

    @GetMapping("/asignaciones")
    public List<AsignacionVehiculo> asignaciones() {
        return gestor.getAsignaciones();
    }

    @PostMapping("/asignaciones")
    public ResponseEntity<Map<String, Object>> crearAsignacion(
            @RequestBody Map<String, Object> datos) {

        Number idVehiculo = numeroObligatorio(datos, "idVehiculo");
        Number idEmpleado = numeroObligatorio(datos, "idEmpleado");

        AsignacionVehiculo asignacion = gestor.asignarVehiculo(
                idVehiculo.intValue(),
                idEmpleado.intValue()
        );

        if (asignacion == null) {
            return error(HttpStatus.CONFLICT,
                    "No se pudo crear la asignacion: el vehiculo o empleado "
                            + "ya tiene una asignacion activa, o alguno no existe o esta inactivo.");
        }

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Asignacion creada correctamente");
        respuesta.put("asignacion", asignacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PutMapping("/asignaciones/{id}/finalizar")
    public ResponseEntity<Map<String, Object>> finalizarAsignacion(
            @PathVariable int id) {

        AsignacionVehiculo asignacion = gestor.buscarAsignacionPorId(id);
        if (asignacion == null) {
            return error(HttpStatus.NOT_FOUND, "Asignacion no encontrada");
        }

        if (!asignacion.isActiva()) {
            return error(HttpStatus.CONFLICT,
                    "La asignacion ya esta finalizada");
        }

        gestor.finalizarAsignacion(asignacion);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Asignacion finalizada correctamente");
        respuesta.put("asignacion", asignacion);
        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/viajes")
    public List<Viaje> viajes() {
        return gestor.getViajes();
    }

    @GetMapping("/eventos-camara")
    public List<String[]> eventosCamara() {
        return EventoCamaraDAO.obtenerTodos();
    }

    @PostMapping("/camara")
    public Map<String, Object> procesarCamara(
            @RequestBody Map<String, String> datos) {

        String matricula = textoObligatorio(datos, "matricula");
        EventoCamara evento = new EventoCamara(matricula);
        gestor.procesarEventoCamara(evento);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", "Evento de camara procesado");
        respuesta.put("matricula", matricula);
        return respuesta;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarSolicitudInvalida(
            IllegalArgumentException excepcion) {
        return error(HttpStatus.BAD_REQUEST, excepcion.getMessage());
    }

    private static String textoObligatorio(
            Map<String, String> datos,
            String campo) {

        String valor = datos.get(campo);
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El campo " + campo + " es obligatorio");
        }
        return valor.trim();
    }

    private static Number numeroObligatorio(
            Map<String, Object> datos,
            String campo) {

        Object valor = datos.get(campo);
        if (!(valor instanceof Number)) {
            throw new IllegalArgumentException(
                    "El campo " + campo + " debe ser numerico");
        }
        return (Number) valor;
    }

    private static ResponseEntity<Map<String, Object>> mensaje(String mensaje) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", mensaje);
        return ResponseEntity.ok(respuesta);
    }

    private static ResponseEntity<Map<String, Object>> error(
            HttpStatus estado,
            String mensaje) {

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("error", mensaje);
        return ResponseEntity.status(estado).body(respuesta);
    }
}
