package control.vehiculos;

import java.time.LocalDateTime;

public class AsignacionVehiculo {

    private final int id;
    private final Vehiculo vehiculo;
    private final Empleado empleado;

    private final LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;

    private boolean activa;

    public AsignacionVehiculo(
            int id,
            Vehiculo vehiculo,
            Empleado empleado) {

        this(
                id,
                vehiculo,
                empleado,
                LocalDateTime.now()
        );
    }

    public AsignacionVehiculo(
            int id,
            Vehiculo vehiculo,
            Empleado empleado,
            LocalDateTime fechaHoraInicio) {

        if (vehiculo == null) {
            throw new IllegalArgumentException(
                    "El vehiculo no puede ser null."
            );
        }

        if (empleado == null) {
            throw new IllegalArgumentException(
                    "El empleado no puede ser null."
            );
        }

        if (fechaHoraInicio == null) {
            throw new IllegalArgumentException(
                    "La fecha de inicio no puede ser null."
            );
        }

        this.id = id;
        this.vehiculo = vehiculo;
        this.empleado = empleado;
        this.fechaHoraInicio = fechaHoraInicio;
        this.activa = true;
    }

    public AsignacionVehiculo(
            int id,
            Vehiculo vehiculo,
            Empleado empleado,
            LocalDateTime fechaHoraInicio,
            LocalDateTime fechaHoraFin,
            boolean activa) {

        if (vehiculo == null) {
            throw new IllegalArgumentException(
                    "El vehiculo no puede ser null."
            );
        }

        if (empleado == null) {
            throw new IllegalArgumentException(
                    "El empleado no puede ser null."
            );
        }

        if (fechaHoraInicio == null) {
            throw new IllegalArgumentException(
                    "La fecha de inicio no puede ser null."
            );
        }

        if (activa && fechaHoraFin != null) {
            throw new IllegalArgumentException(
                    "Una asignacion activa no puede tener fecha de fin."
            );
        }

        if (!activa && fechaHoraFin == null) {
            throw new IllegalArgumentException(
                    "Una asignacion finalizada debe tener fecha de fin."
            );
        }

        this.id = id;
        this.vehiculo = vehiculo;
        this.empleado = empleado;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.activa = activa;
    }

    public void finalizar() {

        if (!activa) {
            throw new IllegalStateException(
                    "La asignacion ya esta finalizada."
            );
        }

        fechaHoraFin = LocalDateTime.now();
        activa = false;
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
