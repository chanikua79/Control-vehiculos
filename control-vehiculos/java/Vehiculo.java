public class Vehiculo {

    private int id;
    private String matricula;
    private String marca;
    private String modelo;
    private boolean activo;

    public Vehiculo(int id, String matricula, String marca, String modelo) {
        this.id = id;
        this.matricula = matricula;
        this.marca = marca;
        this.modelo = modelo;
        this.activo = true;
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

    public void desactivar() {
        activo = false;
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