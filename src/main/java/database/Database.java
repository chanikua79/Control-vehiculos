package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String URL =
            "jdbc:sqlite:" + System.getenv().getOrDefault("VEHICULOS_DB_PATH", "vehiculos.db");

    public static Connection conectar()
            throws SQLException {

        try {

            Class.forName(
                    "org.sqlite.JDBC"
            );

        } catch (ClassNotFoundException e) {

            throw new SQLException(
                    "No se encontro el driver SQLite JDBC.",
                    e
            );
        }

        Connection conexion = DriverManager.getConnection(URL);
        try (var st = conexion.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
            st.execute("PRAGMA busy_timeout = 5000");
            st.execute("PRAGMA journal_mode = WAL");
        }
        return conexion;
    }

    public static void probarConexion() {

        try (Connection conexion = conectar()) {

            System.out.println(
                    "Conexion con SQLite establecida."
            );

        } catch (SQLException e) {

            System.out.println(
                    "Error conectando con SQLite:"
            );

            e.printStackTrace();
        }
    }
}