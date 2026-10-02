package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EventoCamaraDAO {

public static void guardar(
        String matricula,
        String fechaHora) {

    String sql = """
            INSERT INTO eventos_camara
            (matricula, fecha_hora)
            VALUES (?, ?)
            """;

    try (
        Connection conexion = Database.conectar();
        PreparedStatement ps =
                conexion.prepareStatement(sql)
    ) {

        ps.setString(1, matricula);
        ps.setString(2, fechaHora);

        ps.executeUpdate();

    } catch (SQLException e) {

        System.out.println(
                "Error guardando evento de camara:"
        );

        e.printStackTrace();
    }
}

public static List<String[]> obtenerTodos() {

    List<String[]> eventos =
            new ArrayList<>();

    String sql = """
            SELECT id, matricula, fecha_hora
            FROM eventos_camara
            ORDER BY id
            """;

    try (
        Connection conexion = Database.conectar();
        Statement st = conexion.createStatement();
        ResultSet rs = st.executeQuery(sql)
    ) {

        while (rs.next()) {

            eventos.add(
                    new String[]{
                            String.valueOf(
                                    rs.getInt("id")
                            ),
                            rs.getString("matricula"),
                            rs.getString("fecha_hora")
                    }
            );
        }

    } catch (SQLException e) {

        System.out.println(
                "Error leyendo eventos de camara:"
        );

        e.printStackTrace();
    }

    return eventos;
}

}
