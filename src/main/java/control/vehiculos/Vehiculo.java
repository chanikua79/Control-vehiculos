package control.vehiculos;

public class Vehiculo {

    private int id;
    private String matricula;
    private String marca;
    private String modelo;
    private boolean activo;

    public Vehiculo(
            int id,
            String matricula,
            String marca,
            String modelo) {

        this(id, matricula, marca, modelo, true);
    }

    // Constructor utilizado al cargar desde SQLite
    public Vehiculo(
            int id,
            String matricula,
            String marca,
            String modelo,
            boolean activo) {

        this.id = id;
        this.matricula = matricula;
        this.marca = marca;
        this.modelo = modelo;
        this.activo = activo;
    }

    public int getId() {
        return id;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public boolean isActivo() {
        return activo;
    }

        public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

public void desactivar() {
        activo = false;
    }

    public void activar() {
        activo = true;
    }

    @Override
    public String toString() {

        return "Vehiculo{" +
                "id=" + id +
                ", matricula='" + matricula + '\'' +
                ", marca='" + marca + '\'' +
                ", modelo='" + modelo + '\'' +
                ", activo=" + activo +
                '}';
    }
}