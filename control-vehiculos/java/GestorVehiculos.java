import java.util.ArrayList;
import java.util.List;

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
    }

    public void agregarVehiculo(Vehiculo vehiculo) {
        vehiculos.add(vehiculo);
    }

    public List<Vehiculo> getVehiculos() {
        return vehiculos;
    }

    public void agregarEmpleado(Empleado empleado) {
        empleados.add(empleado);
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
                    + asignacionEmpleado.getVehiculo().getMatricula()
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

        return nuevaAsignacion;
    }

    public void procesarEventoCamara(
            EventoCamara evento) {

        String matricula = evento.getMatricula();

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

            AsignacionVehiculo asignacion =
                    buscarAsignacionActivaPorVehiculo(
                            vehiculo.getId()
                    );

            if (asignacion != null) {

                asignacion.finalizar();

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

        if (buscarViajeActivoPorVehiculo(idVehiculo) != null) {
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
}