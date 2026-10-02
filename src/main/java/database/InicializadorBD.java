package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class InicializadorBD {

    public static void crearTablas() {

        try (
            Connection conexion = Database.conectar();
            Statement statement = conexion.createStatement()
        ) {

            String empleados = """
                    CREATE TABLE IF NOT EXISTS empleados (
                        id INTEGER PRIMARY KEY,
                        nombre TEXT NOT NULL,
                        codigo_qr TEXT UNIQUE NOT NULL,
                        activo INTEGER NOT NULL
                    )
                    """;

            String vehiculos = """
                    CREATE TABLE IF NOT EXISTS vehiculos (
                        id INTEGER PRIMARY KEY,
                        matricula TEXT UNIQUE NOT NULL,
                        marca TEXT NOT NULL,
                        modelo TEXT NOT NULL,
                        activo INTEGER NOT NULL
                    )
                    """;

            String asignaciones = """
                    CREATE TABLE IF NOT EXISTS asignaciones (
                        id INTEGER PRIMARY KEY,
                        vehiculo_id INTEGER NOT NULL,
                        empleado_id INTEGER NOT NULL,
                        inicio TEXT NOT NULL,
                        fin TEXT,
                        activa INTEGER NOT NULL
                    )
                    """;

            String viajes = """
                    CREATE TABLE IF NOT EXISTS viajes (
                        id INTEGER PRIMARY KEY,
                        vehiculo_id INTEGER NOT NULL,
                        empleado_id INTEGER NOT NULL,
                        salida TEXT NOT NULL,
                        entrada TEXT,
                        duracion_minutos INTEGER
                    )
                    """;

            String eventosCamara = """
                    CREATE TABLE IF NOT EXISTS eventos_camara (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        matricula TEXT NOT NULL,
                        fecha_hora TEXT NOT NULL
                    )
                    """;

            statement.execute(empleados);
            statement.execute(vehiculos);
            statement.execute(asignaciones);
            statement.execute(viajes);
            statement.execute(eventosCamara);

            System.out.println(
                    "Tablas creadas correctamente."
            );

        } catch (SQLException e) {

            System.out.println(
                    "Error creando las tablas:"
            );

            e.printStackTrace();
        }
    }
}