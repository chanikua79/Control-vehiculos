package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculoDAO {

    public static void guardar(
            int id,
            String matricula,
            String marca,
            String modelo,
            boolean activo) {

        String sql = """
                INSERT OR REPLACE INTO vehiculos
                (id, matricula, marca, modelo, activo)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (
            Connection conexion = Database.conectar();
            PreparedStatement ps =
                    conexion.prepareStatement(sql)
        ) {

            ps.setInt(1, id);
            ps.setString(2, matricula);
            ps.setString(3, marca);
            ps.setString(4, modelo);
            ps.setInt(5, activo ? 1 : 0);

            ps.executeUpdate();

        } catch (SQLException e) {

            System.out.println(
                    "Error guardando vehiculo:"
            );

            e.printStackTrace();
        }
    }

    public static List<String[]> obtenerTodos() {

        List<String[]> vehiculos =
                new ArrayList<>();

        String sql =
                "SELECT id, matricula, marca, modelo, activo " +
                "FROM vehiculos ORDER BY id";

        try (
            Connection conexion = Database.conectar();
            Statement st = conexion.createStatement();
            ResultSet rs = st.executeQuery(sql)
        ) {

            while (rs.next()) {

                vehiculos.add(
                        new String[]{
                                String.valueOf(
                                        rs.getInt("id")
                                ),
                                rs.getString("matricula"),
                                rs.getString("marca"),
                                rs.getString("modelo"),
                                String.valueOf(
                                        rs.getInt("activo")
                                )
                        }
                );
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error leyendo vehiculos:"
            );

            e.printStackTrace();
        }

        return vehiculos;
    }
}
