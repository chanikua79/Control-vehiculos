package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {

    public static void guardar(
            int id,
            String nombre,
            String codigoQR,
            boolean activo) {

        String sql = """
                INSERT OR REPLACE INTO empleados
                (id, nombre, codigo_qr, activo)
                VALUES (?, ?, ?, ?)
                """;

        try (
            Connection conexion = Database.conectar();
            PreparedStatement ps =
                    conexion.prepareStatement(sql)
        ) {

            ps.setInt(1, id);
            ps.setString(2, nombre);
            ps.setString(3, codigoQR);
            ps.setInt(4, activo ? 1 : 0);

            ps.executeUpdate();

        } catch (SQLException e) {

            System.out.println(
                    "Error guardando empleado:"
            );

            e.printStackTrace();
        }
    }

    public static List<String[]> obtenerTodos() {

        List<String[]> empleados =
                new ArrayList<>();

        String sql =
                "SELECT id, nombre, codigo_qr, activo " +
                "FROM empleados ORDER BY id";

        try (
            Connection conexion = Database.conectar();
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery(sql)
        ) {

            while (rs.next()) {

                empleados.add(
                        new String[]{
                                String.valueOf(
                                        rs.getInt("id")
                                ),
                                rs.getString("nombre"),
                                rs.getString("codigo_qr"),
                                String.valueOf(
                                        rs.getInt("activo")
                                )
                        }
                );
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error leyendo empleados:"
            );

            e.printStackTrace();
        }

        return empleados;
    }
}
