import java.util.Scanner;

public class Main {

    private static Scanner scanner =
            new Scanner(System.in);

    public static void main(String[] args) {

        GestorVehiculos gestor =
                new GestorVehiculos();

        cargarDatosIniciales(gestor);

        boolean ejecutando = true;

        while (ejecutando) {

            mostrarMenu();

            String opcion =
                    scanner.nextLine();

            System.out.println();

            switch (opcion) {

                case "1":
                    registrarEmpleado(gestor);
                    break;

                case "2":
                    registrarVehiculo(gestor);
                    break;

                case "3":
                    identificarEmpleado(gestor);
                    break;

                case "4":
                    asignarVehiculo(gestor);
                    break;

                case "5":
                    mostrarVehiculos(gestor);
                    break;

                case "6":
                    mostrarEmpleados(gestor);
                    break;

                case "7":
                    gestor.mostrarViajes();
                    break;

                case "8":
                    simularCamara(gestor);
                    break;

                case "9":
                    gestor.mostrarAsignaciones();
                    break;

                case "10":
                    ejecutando = false;
                    System.out.println(
                            "Saliendo del sistema..."
                    );
                    break;

                default:
                    System.out.println(
                            "Opcion no valida."
                    );
            }

            if (ejecutando) {

                System.out.println();
                System.out.println(
                        "Presione ENTER para continuar..."
                );

                scanner.nextLine();
            }
        }

        scanner.close();
    }

    private static void mostrarMenu() {

        System.out.println();
        System.out.println(
                "========================================"
        );
        System.out.println(
                "   SISTEMA DE CONTROL DE VEHICULOS"
        );
        System.out.println(
                "========================================"
        );
        System.out.println(
                "1. Registrar empleado"
        );
        System.out.println(
                "2. Registrar vehiculo"
        );
        System.out.println(
                "3. Identificar empleado por QR"
        );
        System.out.println(
                "4. Asignar vehiculo"
        );
        System.out.println(
                "5. Ver vehiculos"
        );
        System.out.println(
                "6. Ver empleados"
        );
        System.out.println(
                "7. Ver historial de viajes"
        );
        System.out.println(
                "8. Simular camara"
        );
        System.out.println(
                "9. Ver asignaciones"
        );
        System.out.println(
                "10. Salir"
        );
        System.out.println(
                "========================================"
        );

        System.out.print(
                "Seleccione una opcion: "
        );
    }

    private static void cargarDatosIniciales(
            GestorVehiculos gestor) {

        gestor.agregarVehiculo(
                new Vehiculo(
                        1,
                        "ABC-123",
                        "Toyota",
                        "Hilux"
                )
        );

        gestor.agregarVehiculo(
                new Vehiculo(
                        2,
                        "DEF-456",
                        "Ford",
                        "Ranger"
                )
        );

        gestor.agregarVehiculo(
                new Vehiculo(
                        3,
                        "GHI-789",
                        "Chevrolet",
                        "Colorado"
                )
        );

        gestor.agregarEmpleado(
                new Empleado(
                        1,
                        "Carlos Perez",
                        "QR-CARLOS-001"
                )
        );

        gestor.agregarEmpleado(
                new Empleado(
                        2,
                        "Maria Gomez",
                        "QR-MARIA-002"
                )
        );

        gestor.agregarEmpleado(
                new Empleado(
                        3,
                        "Pedro Rodriguez",
                        "QR-PEDRO-003"
                )
        );
    }

    private static void registrarEmpleado(
            GestorVehiculos gestor) {

        System.out.println(
                "=== REGISTRAR EMPLEADO ==="
        );

        System.out.print("ID: ");
        int id = leerEntero();

        System.out.print("Nombre: ");
        String nombre = scanner.nextLine();

        System.out.print("Codigo QR: ");
        String codigoQR = scanner.nextLine();

        Empleado empleado =
                new Empleado(
                        id,
                        nombre,
                        codigoQR
                );

        gestor.agregarEmpleado(empleado);

        System.out.println();
        System.out.println(
                "Empleado registrado correctamente."
        );

        System.out.println(empleado);
    }

    private static void registrarVehiculo(
            GestorVehiculos gestor) {

        System.out.println(
                "=== REGISTRAR VEHICULO ==="
        );

        System.out.print("ID: ");
        int id = leerEntero();

        System.out.print("Matricula: ");
        String matricula = scanner.nextLine();

        System.out.print("Marca: ");
        String marca = scanner.nextLine();

        System.out.print("Modelo: ");
        String modelo = scanner.nextLine();

        Vehiculo vehiculo =
                new Vehiculo(
                        id,
                        matricula,
                        marca,
                        modelo
                );

        gestor.agregarVehiculo(vehiculo);

        System.out.println();
        System.out.println(
                "Vehiculo registrado correctamente."
        );

        System.out.println(vehiculo);
    }

    private static void identificarEmpleado(
            GestorVehiculos gestor) {

        System.out.println(
                "=== IDENTIFICACION POR QR ==="
        );

        System.out.print(
                "Codigo QR: "
        );

        String codigoQR =
                scanner.nextLine();

        IdentificadorEmpleado identificador =
                new IdentificadorQR(
                        gestor.getEmpleados()
                );

        Empleado empleado =
                identificador.identificar(
                        codigoQR
                );

        System.out.println();

        if (empleado == null) {

            System.out.println(
                    "Empleado no identificado."
            );

        } else {

            System.out.println(
                    "Empleado identificado:"
            );

            System.out.println(empleado);
        }
    }

    private static void asignarVehiculo(
            GestorVehiculos gestor) {

        System.out.println(
                "=== ASIGNAR VEHICULO ==="
        );

        System.out.print(
                "ID del vehiculo: "
        );

        int idVehiculo = leerEntero();

        System.out.print(
                "ID del empleado: "
        );

        int idEmpleado = leerEntero();

        AsignacionVehiculo asignacion =
                gestor.asignarVehiculo(
                        idVehiculo,
                        idEmpleado
                );

        System.out.println();

        if (asignacion == null) {

            System.out.println(
                    "No se pudo realizar la asignacion."
            );

        } else {

            System.out.println(
                    "Vehiculo asignado correctamente."
            );

            System.out.println(asignacion);
        }
    }

    private static void mostrarVehiculos(
            GestorVehiculos gestor) {

        System.out.println(
                "=== VEHICULOS ==="
        );

        for (Vehiculo vehiculo :
                gestor.getVehiculos()) {

            System.out.println(vehiculo);
        }
    }

    private static void mostrarEmpleados(
            GestorVehiculos gestor) {

        System.out.println(
                "=== EMPLEADOS ==="
        );

        for (Empleado empleado :
                gestor.getEmpleados()) {

            System.out.println(empleado);
        }
    }

    private static void simularCamara(
        GestorVehiculos gestor) {

    System.out.println(
            "=== CAMARA ==="
    );

    System.out.print(
            "Matricula detectada por la camara: "
    );

    String matricula =
            scanner.nextLine().trim().toUpperCase();

    if (matricula.isEmpty()) {

        System.out.println(
                "No se recibio ninguna matricula."
        );

        return;
    }

    EventoCamara evento =
            new EventoCamara(matricula);

    System.out.println();
    System.out.println(
            "Evento recibido:"
    );

    System.out.println(evento);

    System.out.println();

    gestor.procesarEventoCamara(evento);
    }

    private static int leerEntero() {

        while (true) {

            try {

                String texto =
                        scanner.nextLine();

                return Integer.parseInt(texto);

            } catch (
                    NumberFormatException e) {

                System.out.print(
                        "Introduzca un numero valido: "
                );
            }
        }
    }
}