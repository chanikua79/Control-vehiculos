package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AsignacionDAO {

    public static void guardar(
            int id,
            int vehiculoId,
            int empleadoId,
            String inicio,
            String fin,
            boolean activa) {

        String sql = """
                INSERT OR REPLACE INTO asignaciones
                (id, vehiculo_id, empleado_id, inicio, fin, activa)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (
            Connection conexion = Database.conectar();
            PreparedStatement ps =
                    conexion.prepareStatement(sql)
        ) {

            ps.setInt(1, id);
            ps.setInt(2, vehiculoId);
            ps.setInt(3, empleadoId);
            ps.setString(4, inicio);
            ps.setString(5, fin);
            ps.setInt(6, activa ? 1 : 0);

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error guardando asignacion:");
            e.printStackTrace();
        }
    }

    public static List<String[]> obtenerTodos() {

        List<String[]> asignaciones = new ArrayList<>();

        String sql = """
                SELECT id, vehiculo_id, empleado_id,
                       inicio, fin, activa
                FROM asignaciones
                ORDER BY id
                """;

        try (
            Connection conexion = Database.conectar();
            PreparedStatement ps =
                    conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                asignaciones.add(
                        new String[]{
                                String.valueOf(rs.getInt("id")),
                                String.valueOf(rs.getInt("vehiculo_id")),
                                String.valueOf(rs.getInt("empleado_id")),
                                rs.getString("inicio"),
                                rs.getString("fin"),
                                String.valueOf(rs.getInt("activa"))
                        }
                );
            }

        } catch (SQLException e) {
            System.out.println("Error leyendo asignaciones:");
            e.printStackTrace();
        }

        return asignaciones;
    }
}
