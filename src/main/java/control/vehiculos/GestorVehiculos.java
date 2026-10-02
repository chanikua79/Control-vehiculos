package control.vehiculos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import database.AsignacionDAO;
import database.EmpleadoDAO;
import database.EventoCamaraDAO;
import database.InicializadorBD;
import database.VehiculoDAO;
import database.ViajeDAO;
import org.springframework.stereotype.Service;

@Service
public class GestorVehiculos {

private List<Vehiculo> vehiculos;
private List<Empleado> empleados;
private List<Viaje> viajes;
private List<AsignacionVehiculo> asignaciones;

private int siguienteViajeId = 1;
private int siguienteAsignacionId = 1;

public GestorVehiculos() {
    vehiculos = new ArrayList<>();
    empleados = new ArrayList<>();
    viajes = new ArrayList<>();
    asignaciones = new ArrayList<>();

    cargarDatos();
}

private void cargarDatos() {

    InicializadorBD.crearTablas();

    for (String[] datos : VehiculoDAO.obtenerTodos()) {

        Vehiculo vehiculo = new Vehiculo(
                Integer.parseInt(datos[0]),
                datos[1],
                datos[2],
                datos[3],
                "1".equals(datos[4])
        );

        vehiculos.add(vehiculo);
    }

    for (String[] datos : EmpleadoDAO.obtenerTodos()) {

        Empleado empleado = new Empleado(
                Integer.parseInt(datos[0]),
                datos[1],
                datos[2],
                "1".equals(datos[3])
        );

        empleados.add(empleado);
    }

    for (String[] datos : AsignacionDAO.obtenerTodos()) {

        Vehiculo vehiculo = buscarVehiculo(
                Integer.parseInt(datos[1])
        );

        Empleado empleado = buscarEmpleado(
                Integer.parseInt(datos[2])
        );

        if (vehiculo == null || empleado == null) {
            continue;
        }

        LocalDateTime inicio =
                LocalDateTime.parse(datos[3]);

        LocalDateTime fin =
                datos[4] == null
                        ? null
                        : LocalDateTime.parse(datos[4]);

        boolean activa = "1".equals(datos[5]);

        AsignacionVehiculo asignacion =
                new AsignacionVehiculo(
                        Integer.parseInt(datos[0]),
                        vehiculo,
                        empleado,
                        inicio,
                        fin,
                        activa
                );

        asignaciones.add(asignacion);

        if (asignacion.getId() >= siguienteAsignacionId) {
            siguienteAsignacionId =
                    asignacion.getId() + 1;
        }
    }

    for (String[] datos : ViajeDAO.obtenerTodos()) {

        Vehiculo vehiculo = buscarVehiculo(
                Integer.parseInt(datos[1])
        );

        Empleado empleado = buscarEmpleado(
                Integer.parseInt(datos[2])
        );

        if (vehiculo == null || empleado == null) {
            continue;
        }

        LocalDateTime salida =
                LocalDateTime.parse(datos[3]);

        LocalDateTime entrada =
                datos[4] == null
                        ? null
                        : LocalDateTime.parse(datos[4]);

        Viaje viaje =
                new Viaje(
                        Integer.parseInt(datos[0]),
                        vehiculo,
                        empleado,
                        salida,
                        entrada
                );

        viajes.add(viaje);

        if (viaje.getId() >= siguienteViajeId) {
            siguienteViajeId =
                    viaje.getId() + 1;
        }
    }
}

public void agregarVehiculo(Vehiculo vehiculo) {

    vehiculos.add(vehiculo);

    VehiculoDAO.guardar(
            vehiculo.getId(),
            vehiculo.getMatricula(),
            vehiculo.getMarca(),
            vehiculo.getModelo(),
            vehiculo.isActivo()
    );
}

public List<Vehiculo> getVehiculos() {
    return vehiculos;
}

public void agregarEmpleado(Empleado empleado) {

    empleados.add(empleado);

    EmpleadoDAO.guardar(
            empleado.getId(),
            empleado.getNombre(),
            empleado.getCodigoQR(),
            empleado.isActivo()
    );
}

public List<Empleado> getEmpleados() {
    return empleados;
}

public AsignacionVehiculo asignarVehiculo(
        int idVehiculo,
        int idEmpleado) {

    Vehiculo vehiculo = buscarVehiculo(idVehiculo);
    Empleado empleado = buscarEmpleado(idEmpleado);

    if (vehiculo == null) {
        System.out.println("Vehiculo no encontrado.");
        return null;
    }

    if (empleado == null) {
        System.out.println("Empleado no encontrado.");
        return null;
    }

    if (!vehiculo.isActivo()) {
        System.out.println("El vehiculo no esta activo.");
        return null;
    }

    if (!empleado.isActivo()) {
        System.out.println("El empleado no esta activo.");
        return null;
    }

    AsignacionVehiculo asignacionVehiculo =
            buscarAsignacionActivaPorVehiculo(idVehiculo);

    if (asignacionVehiculo != null) {

        System.out.println(
                "El vehiculo ya esta asignado a "
                + asignacionVehiculo.getEmpleado().getNombre()
        );

        return null;
    }

    AsignacionVehiculo asignacionEmpleado =
            buscarAsignacionActivaPorEmpleado(idEmpleado);

    if (asignacionEmpleado != null) {

        System.out.println(
                "El empleado ya tiene asignado "
                + asignacionEmpleado
                        .getVehiculo()
                        .getMatricula()
        );

        return null;
    }

    AsignacionVehiculo nuevaAsignacion =
            new AsignacionVehiculo(
                    siguienteAsignacionId++,
                    vehiculo,
                    empleado
            );

    asignaciones.add(nuevaAsignacion);

    AsignacionDAO.guardar(
            nuevaAsignacion.getId(),
            vehiculo.getId(),
            empleado.getId(),
            nuevaAsignacion.getFechaHoraInicio().toString(),
            null,
            true
    );

    return nuevaAsignacion;
}

public void procesarEventoCamara(
        EventoCamara evento) {

    String matricula = evento.getMatricula();

    // Guardar la deteccion en SQLite
    EventoCamaraDAO.guardar(
            matricula,
            java.time.LocalDateTime.now().toString()
    );

    Vehiculo vehiculo =
            buscarVehiculoPorMatricula(matricula);

    if (vehiculo == null) {

        System.out.println(
                "CAMARA: Vehiculo no registrado: "
                + matricula
        );

        return;
    }

    Viaje viajeActivo =
            buscarViajeActivoPorVehiculo(
                    vehiculo.getId()
            );

    // ENTRADA
    if (viajeActivo != null) {

        System.out.println(
                "CAMARA: "
                + matricula
                + " -> ENTRADA"
        );

        viajeActivo.registrarEntrada();

        ViajeDAO.guardar(
                viajeActivo.getId(),
                viajeActivo.getVehiculo().getId(),
                viajeActivo.getEmpleado().getId(),
                viajeActivo.getSalida().toString(),
                viajeActivo.getEntrada().toString(),
                viajeActivo.obtenerDuracionMinutos()
        );

        AsignacionVehiculo asignacion =
                buscarAsignacionActivaPorVehiculo(
                        vehiculo.getId()
                );

        if (asignacion != null) {

            asignacion.finalizar();

            AsignacionDAO.guardar(
                    asignacion.getId(),
                    asignacion.getVehiculo().getId(),
                    asignacion.getEmpleado().getId(),
                    asignacion.getFechaHoraInicio().toString(),
                    asignacion.getFechaHoraFin().toString(),
                    false
            );

            System.out.println(
                    "Asignacion finalizada."
            );
        }

        return;
    }

    // SALIDA
    AsignacionVehiculo asignacion =
            buscarAsignacionActivaPorVehiculo(
                    vehiculo.getId()
            );

    if (asignacion == null) {

        System.out.println(
                "CAMARA: "
                + matricula
                + " -> SALIDA RECHAZADA"
        );

        System.out.println(
                "El vehiculo no tiene una asignacion activa."
        );

        return;
    }

    Empleado empleado =
            asignacion.getEmpleado();

    System.out.println(
            "CAMARA: "
            + matricula
            + " -> SALIDA"
    );

    System.out.println(
            "Empleado autorizado: "
            + empleado.getNombre()
    );

    registrarSalida(
            vehiculo.getId(),
            empleado.getId()
    );
}

public Viaje registrarSalida(
        int idVehiculo,
        int idEmpleado) {

    Vehiculo vehiculo =
            buscarVehiculo(idVehiculo);

    Empleado empleado =
            buscarEmpleado(idEmpleado);

    if (vehiculo == null) {
        System.out.println(
                "Vehiculo no encontrado."
        );
        return null;
    }

    if (empleado == null) {
        System.out.println(
                "Empleado no encontrado."
        );
        return null;
    }

    if (buscarViajeActivoPorVehiculo(
            idVehiculo) != null) {

        System.out.println(
                "El vehiculo ya se encuentra fuera."
        );

        return null;
    }

    Viaje viaje =
            new Viaje(
                    siguienteViajeId++,
                    vehiculo,
                    empleado
            );

    viajes.add(viaje);

    ViajeDAO.guardar(
            viaje.getId(),
            vehiculo.getId(),
            empleado.getId(),
            viaje.getSalida().toString(),
            null,
            0
    );

    return viaje;
}

private Vehiculo buscarVehiculo(int id) {

    for (Vehiculo vehiculo : vehiculos) {

        if (vehiculo.getId() == id) {
            return vehiculo;
        }
    }

    return null;
}

private Vehiculo buscarVehiculoPorMatricula(
        String matricula) {

    for (Vehiculo vehiculo : vehiculos) {

        if (vehiculo.getMatricula()
                .equalsIgnoreCase(matricula)) {

            return vehiculo;
        }
    }

    return null;
}

private Empleado buscarEmpleado(int id) {

    for (Empleado empleado : empleados) {

        if (empleado.getId() == id) {
            return empleado;
        }
    }

    return null;
}

private AsignacionVehiculo
buscarAsignacionActivaPorVehiculo(
        int idVehiculo) {

    for (AsignacionVehiculo asignacion :
            asignaciones) {

        if (asignacion.isActiva()
                && asignacion
                .getVehiculo()
                .getId() == idVehiculo) {

            return asignacion;
        }
    }

    return null;
}

private AsignacionVehiculo
buscarAsignacionActivaPorEmpleado(
        int idEmpleado) {

    for (AsignacionVehiculo asignacion :
            asignaciones) {

        if (asignacion.isActiva()
                && asignacion
                .getEmpleado()
                .getId() == idEmpleado) {

            return asignacion;
        }
    }

    return null;
}

private Viaje buscarViajeActivoPorVehiculo(
        int idVehiculo) {

    for (Viaje viaje : viajes) {

        if (viaje.getVehiculo().getId()
                == idVehiculo
                && viaje.estaActivo()) {

            return viaje;
        }
    }

    return null;
}

public void mostrarViajes() {

    System.out.println();
    System.out.println(
            "=== HISTORIAL DE VIAJES ==="
    );

    if (viajes.isEmpty()) {

        System.out.println(
                "No existen viajes registrados."
        );

        return;
    }

    for (Viaje viaje : viajes) {
        System.out.println(viaje);
    }
}

public void mostrarAsignaciones() {

    System.out.println();
    System.out.println(
            "=== ASIGNACIONES ==="
    );

    if (asignaciones.isEmpty()) {

        System.out.println(
                "No existen asignaciones."
        );

        return;
    }

    for (AsignacionVehiculo asignacion :
            asignaciones) {

        System.out.println(asignacion);
    }
}

public void mostrarEventosCamara() {

System.out.println();
System.out.println("=== HISTORIAL DE CAMARA ===");

List<String[]> eventos =
        database.EventoCamaraDAO.obtenerTodos();

if (eventos.isEmpty()) {

    System.out.println(
            "No existen eventos de camara."
    );

    return;
}

for (String[] evento : eventos) {

    System.out.println(
            "ID: " + evento[0]
            + " | Matricula: " + evento[1]
            + " | Fecha/Hora: " + evento[2]
    );
}


}


    public Vehiculo buscarVehiculoPorMatriculaPublica(String matricula) {
        return buscarVehiculoPorMatricula(matricula);
    }

    public int obtenerSiguienteVehiculoId() {
        int maxId = 0;

        for (Vehiculo vehiculo : vehiculos) {
            if (vehiculo.getId() > maxId) {
                maxId = vehiculo.getId();
            }
        }

        return maxId + 1;
    }

    public boolean editarVehiculo(
            int id,
            String matricula,
            String marca,
            String modelo) {

        Vehiculo vehiculo = buscarVehiculo(id);

        if (vehiculo == null) {
            return false;
        }

        vehiculo.setMatricula(matricula);
        vehiculo.setMarca(marca);
        vehiculo.setModelo(modelo);

        VehiculoDAO.guardar(
                vehiculo.getId(),
                vehiculo.getMatricula(),
                vehiculo.getMarca(),
                vehiculo.getModelo(),
                vehiculo.isActivo()
        );

        return true;
    }

    public boolean cambiarEstadoVehiculo(int id, Boolean activo) {

        Vehiculo vehiculo = buscarVehiculo(id);

        if (vehiculo == null) {
            return false;
        }

        if (Boolean.TRUE.equals(activo)) {
            vehiculo.activar();
        } else {
            vehiculo.desactivar();
        }

        VehiculoDAO.guardar(
                vehiculo.getId(),
                vehiculo.getMatricula(),
                vehiculo.getMarca(),
                vehiculo.getModelo(),
                vehiculo.isActivo()
        );

        return true;
    }

    public Empleado buscarEmpleadoPorQR(String codigoQR) {

        for (Empleado empleado : empleados) {
            if (empleado.getCodigoQR().equalsIgnoreCase(codigoQR)) {
                return empleado;
            }
        }

        return null;
    }

    public int obtenerSiguienteEmpleadoId() {
        int maxId = 0;

        for (Empleado empleado : empleados) {
            if (empleado.getId() > maxId) {
                maxId = empleado.getId();
            }
        }

        return maxId + 1;
    }

    public boolean editarEmpleado(
            int id,
            String nombre,
            String codigoQR) {

        Empleado empleado = buscarEmpleado(id);

        if (empleado == null) {
            return false;
        }

        empleado.setNombre(nombre);
        empleado.setCodigoQR(codigoQR);

        EmpleadoDAO.guardar(
                empleado.getId(),
                empleado.getNombre(),
                empleado.getCodigoQR(),
                empleado.isActivo()
        );

        return true;
    }

    public boolean cambiarEstadoEmpleado(int id, Boolean activo) {

        Empleado empleado = buscarEmpleado(id);

        if (empleado == null) {
            return false;
        }

        if (Boolean.TRUE.equals(activo)) {
            empleado.activar();
        } else {
            empleado.desactivar();
        }

        EmpleadoDAO.guardar(
                empleado.getId(),
                empleado.getNombre(),
                empleado.getCodigoQR(),
                empleado.isActivo()
        );

        return true;
    }


    public boolean existeVehiculo(int id) {
        return buscarVehiculo(id) != null;
    }

    public boolean existeOtraMatricula(String matricula, int idExcluir) {
        Vehiculo existente = buscarVehiculoPorMatricula(matricula);
        return existente != null && existente.getId() != idExcluir;
    }

    public boolean existeEmpleado(int id) {
        return buscarEmpleado(id) != null;
    }

    public boolean existeOtroCodigoQR(String codigoQR, int idExcluir) {
        Empleado existente = buscarEmpleadoPorQR(codigoQR);
        return existente != null && existente.getId() != idExcluir;
    }

    public AsignacionVehiculo buscarAsignacionPorId(int id) {
        for (AsignacionVehiculo asignacion : asignaciones) {
            if (asignacion.getId() == id) {
                return asignacion;
            }
        }
        return null;
    }

    public void finalizarAsignacion(AsignacionVehiculo asignacion) {
        if (asignacion == null || !asignacion.isActiva()) {
            throw new IllegalArgumentException(
                    "La asignacion no existe o ya esta finalizada");
        }

        asignacion.finalizar();

        AsignacionDAO.guardar(
                asignacion.getId(),
                asignacion.getVehiculo().getId(),
                asignacion.getEmpleado().getId(),
                asignacion.getFechaHoraInicio().toString(),
                asignacion.getFechaHoraFin().toString(),
                false
        );
    }

    public List<Viaje> getViajes() {
        return viajes;
    }

    public List<AsignacionVehiculo> getAsignaciones() {
        return asignaciones;
    }

}

