public class IdentificadorQR implements IdentificadorEmpleado {

    private java.util.List<Empleado> empleados;

    public IdentificadorQR(java.util.List<Empleado> empleados) {
        this.empleados = empleados;
    }

    @Override
    public Empleado identificar(String codigoQR) {

        for (Empleado empleado : empleados) {

            if (empleado.getCodigoQR().equals(codigoQR)) {
                return empleado;
            }
        }

        return null;
    }
}