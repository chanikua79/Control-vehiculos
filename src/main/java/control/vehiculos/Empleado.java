package control.vehiculos;

public class Empleado {

    private int id;
    private String nombre;
    private String codigoQR;
    private boolean activo;

    public Empleado(
            int id,
            String nombre,
            String codigoQR) {

        this(id, nombre, codigoQR, true);
    }

    // Constructor utilizado al cargar desde SQLite
    public Empleado(
            int id,
            String nombre,
            String codigoQR,
            boolean activo) {

        this.id = id;
        this.nombre = nombre;
        this.codigoQR = codigoQR;
        this.activo = activo;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCodigoQR() {
        return codigoQR;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCodigoQR(String codigoQR) {
        this.codigoQR = codigoQR;
    }

public void desactivar() {
        activo = false;
    }

    public void activar() {
        activo = true;
    }

    @Override
    public String toString() {

        return "Empleado{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", codigoQR='" + codigoQR + '\'' +
                ", activo=" + activo +
                '}';
    }
}