import java.time.LocalDateTime;

public class AsignacionVehiculo {

    private int id;
    private Vehiculo vehiculo;
    private Empleado empleado;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private boolean activa;

    public AsignacionVehiculo(
            int id,
            Vehiculo vehiculo,
            Empleado empleado) {

        this.id = id;
        this.vehiculo = vehiculo;
        this.empleado = empleado;
        this.fechaHoraInicio = LocalDateTime.now();
        this.activa = true;
        this.fechaHoraFin = null;
    }

    public int getId() {
        return id;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public boolean isActiva() {
        return activa;
    }

    public void finalizar() {

        if (!activa) {
            return;
        }

        activa = false;
        fechaHoraFin = LocalDateTime.now();
    }

    @Override
    public String toString() {

        return "Asignacion{" +
                "id=" + id +
                ", vehiculo=" + vehiculo.getMatricula() +
                ", empleado=" + empleado.getNombre() +
                ", inicio=" + fechaHoraInicio +
                ", fin=" + fechaHoraFin +
                ", activa=" + activa +
                '}';
    }
}