package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ViajeDAO {

    public static void guardar(
            int id,
            int vehiculoId,
            int empleadoId,
            String salida,
            String entrada,
            long duracionMinutos) {

        String sql = """
                INSERT OR REPLACE INTO viajes
                (id, vehiculo_id, empleado_id,
                 salida, entrada, duracion_minutos)
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
            ps.setString(4, salida);

            if (entrada == null) {
                ps.setNull(5, java.sql.Types.VARCHAR);
            } else {
                ps.setString(5, entrada);
            }

            ps.setLong(6, duracionMinutos);

            ps.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error guardando viaje:");
            e.printStackTrace();
        }
    }

    public static List<String[]> obtenerTodos() {

        List<String[]> viajes = new ArrayList<>();

        String sql = """
                SELECT id, vehiculo_id, empleado_id,
                       salida, entrada, duracion_minutos
                FROM viajes
                ORDER BY id
                """;

        try (
            Connection conexion = Database.conectar();
            PreparedStatement ps =
                    conexion.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                viajes.add(
                        new String[]{
                                String.valueOf(rs.getInt("id")),
                                String.valueOf(rs.getInt("vehiculo_id")),
                                String.valueOf(rs.getInt("empleado_id")),
                                rs.getString("salida"),
                                rs.getString("entrada"),
                                String.valueOf(
                                        rs.getLong("duracion_minutos")
                                )
                        }
                );
            }

        } catch (SQLException e) {
            System.out.println("Error leyendo viajes:");
            e.printStackTrace();
        }

        return viajes;
    }
}
