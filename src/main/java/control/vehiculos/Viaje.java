package control.vehiculos;

import java.time.Duration;
import java.time.LocalDateTime;

public class Viaje {

    private int id;
    private Vehiculo vehiculo;
    private Empleado empleado;

    private LocalDateTime salida;
    private LocalDateTime entrada;

    // Constructor para nuevos viajes
    public Viaje(
            int id,
            Vehiculo vehiculo,
            Empleado empleado) {

        this.id = id;
        this.vehiculo = vehiculo;
        this.empleado = empleado;
        this.salida = LocalDateTime.now();
        this.entrada = null;
    }

    // Constructor para cargar desde SQLite
    public Viaje(
            int id,
            Vehiculo vehiculo,
            Empleado empleado,
            LocalDateTime salida,
            LocalDateTime entrada) {

        this.id = id;
        this.vehiculo = vehiculo;
        this.empleado = empleado;
        this.salida = salida;
        this.entrada = entrada;
    }

    public void registrarEntrada() {

        if (entrada != null) {

            System.out.println(
                    "El viaje ya tiene una entrada registrada."
            );

            return;
        }

        entrada = LocalDateTime.now();
    }

    public boolean estaActivo() {
        return entrada == null;
    }

    public long obtenerDuracionMinutos() {

        if (entrada == null) {

            return Duration.between(
                    salida,
                    LocalDateTime.now()
            ).toMinutes();
        }

        return Duration.between(
                salida,
                entrada
        ).toMinutes();
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

    public LocalDateTime getSalida() {
        return salida;
    }

    public LocalDateTime getEntrada() {
        return entrada;
    }

    @Override
    public String toString() {

        return "Viaje{" +
                "id=" + id +
                ", vehiculo=" +
                vehiculo.getMatricula() +
                ", empleado=" +
                empleado.getNombre() +
                ", salida=" + salida +
                ", entrada=" + entrada +
                ", duracionMinutos=" +
                obtenerDuracionMinutos() +
                '}';
    }
}
