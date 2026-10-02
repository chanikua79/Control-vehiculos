package advanced;

import database.Database;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public final class AdvancedDAO {
    private AdvancedDAO() {}

    public static void initialize() {
        String[] sql = {
            "CREATE TABLE IF NOT EXISTS llaves_vehiculo (id INTEGER PRIMARY KEY AUTOINCREMENT, vehiculo_id INTEGER NOT NULL, codigo TEXT UNIQUE NOT NULL, estado TEXT NOT NULL DEFAULT 'DISPONIBLE', entregada_a INTEGER, entregada_en TEXT, devuelta_en TEXT, observaciones TEXT)",
            "CREATE TABLE IF NOT EXISTS inspecciones_danos (id INTEGER PRIMARY KEY AUTOINCREMENT, vehiculo_id INTEGER NOT NULL, empleado_id INTEGER, viaje_id INTEGER, momento TEXT NOT NULL, tipo TEXT NOT NULL, observaciones TEXT, fotos_json TEXT, resultado TEXT NOT NULL DEFAULT 'SIN_DANOS')",
            "CREATE TABLE IF NOT EXISTS geocercas (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, latitud REAL NOT NULL, longitud REAL NOT NULL, radio_metros REAL NOT NULL, activa INTEGER NOT NULL DEFAULT 1)",
            "CREATE TABLE IF NOT EXISTS reglas_flotas (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT UNIQUE NOT NULL, evento TEXT NOT NULL, condicion TEXT NOT NULL, accion TEXT NOT NULL, severidad TEXT NOT NULL DEFAULT 'AVISO', activa INTEGER NOT NULL DEFAULT 1)",
            "CREATE TABLE IF NOT EXISTS horarios_operacion (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, dia_semana INTEGER NOT NULL, inicio TEXT NOT NULL, fin TEXT NOT NULL, requiere_autorizacion INTEGER NOT NULL DEFAULT 0, activa INTEGER NOT NULL DEFAULT 1)",
            "CREATE TABLE IF NOT EXISTS feriados (id INTEGER PRIMARY KEY AUTOINCREMENT, fecha TEXT UNIQUE NOT NULL, nombre TEXT NOT NULL, requiere_autorizacion INTEGER NOT NULL DEFAULT 1)",
            "CREATE TABLE IF NOT EXISTS aprobaciones (id INTEGER PRIMARY KEY AUTOINCREMENT, tipo TEXT NOT NULL, referencia_id INTEGER NOT NULL, solicitante TEXT NOT NULL, aprobador TEXT, estado TEXT NOT NULL DEFAULT 'PENDIENTE', creado_en TEXT NOT NULL, resuelto_en TEXT, comentario TEXT)",
            "CREATE TABLE IF NOT EXISTS notificaciones (id INTEGER PRIMARY KEY AUTOINCREMENT, usuario TEXT, titulo TEXT NOT NULL, mensaje TEXT NOT NULL, severidad TEXT NOT NULL DEFAULT 'INFO', creado_en TEXT NOT NULL, leida INTEGER NOT NULL DEFAULT 0)",
            "CREATE TABLE IF NOT EXISTS fotos_evento (id INTEGER PRIMARY KEY AUTOINCREMENT, entidad TEXT NOT NULL, entidad_id INTEGER NOT NULL, tipo TEXT NOT NULL, referencia TEXT NOT NULL, creado_en TEXT NOT NULL)",
            "CREATE TABLE IF NOT EXISTS tokens_temporales (id INTEGER PRIMARY KEY AUTOINCREMENT, sujeto TEXT NOT NULL, token_hash TEXT UNIQUE NOT NULL, expira_en TEXT NOT NULL, usado INTEGER NOT NULL DEFAULT 0, creado_en TEXT NOT NULL)",
            "CREATE TABLE IF NOT EXISTS dispositivos (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, tipo TEXT NOT NULL, identificador TEXT UNIQUE NOT NULL, ubicacion TEXT, activa INTEGER NOT NULL DEFAULT 1, ultimo_contacto TEXT)",
            "CREATE TABLE IF NOT EXISTS configuracion_modulos (modulo TEXT PRIMARY KEY, habilitado INTEGER NOT NULL DEFAULT 1, configuracion_json TEXT NOT NULL DEFAULT '{}')",
            "CREATE TABLE IF NOT EXISTS api_keys (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, clave_hash TEXT UNIQUE NOT NULL, permisos TEXT NOT NULL, expira_en TEXT, activa INTEGER NOT NULL DEFAULT 1, creado_en TEXT NOT NULL)",
            "CREATE TABLE IF NOT EXISTS suscripciones (id INTEGER PRIMARY KEY AUTOINCREMENT, empresa_id INTEGER NOT NULL, plan TEXT NOT NULL, estado TEXT NOT NULL DEFAULT 'PRUEBA', inicio TEXT NOT NULL, fin TEXT, limite_vehiculos INTEGER DEFAULT 10, limite_usuarios INTEGER DEFAULT 5)",
            "CREATE TABLE IF NOT EXISTS telemetria_eventos (id INTEGER PRIMARY KEY AUTOINCREMENT, vehiculo_id INTEGER, tipo TEXT NOT NULL, valor REAL, unidad TEXT, fecha_hora TEXT NOT NULL, origen TEXT NOT NULL)",
            "CREATE TABLE IF NOT EXISTS perfiles_conductor (empleado_id INTEGER PRIMARY KEY, licencia_numero TEXT, categoria TEXT, licencia_vencimiento TEXT, restricciones TEXT, telefono TEXT, correo TEXT)",
            "CREATE TABLE IF NOT EXISTS rutas_gps (id INTEGER PRIMARY KEY AUTOINCREMENT, vehiculo_id INTEGER NOT NULL, nombre TEXT NOT NULL, inicio TEXT, fin TEXT, distancia_km REAL DEFAULT 0, duracion_minutos INTEGER DEFAULT 0, puntos_json TEXT NOT NULL)",
            "CREATE INDEX IF NOT EXISTS idx_llaves_vehiculo ON llaves_vehiculo(vehiculo_id,estado)",
            "CREATE INDEX IF NOT EXISTS idx_inspecciones_vehiculo ON inspecciones_danos(vehiculo_id,momento)",
            "CREATE INDEX IF NOT EXISTS idx_gps_rutas_vehiculo ON rutas_gps(vehiculo_id,inicio)",
            "CREATE INDEX IF NOT EXISTS idx_notificaciones_usuario ON notificaciones(usuario,leida,creado_en)",
            "CREATE INDEX IF NOT EXISTS idx_aprobaciones_estado ON aprobaciones(estado,creado_en)",
            "CREATE INDEX IF NOT EXISTS idx_telemetria_vehiculo ON telemetria_eventos(vehiculo_id,fecha_hora)"
        };
        try(Connection c=Database.conectar(); Statement s=c.createStatement()) { for(String q:sql)s.executeUpdate(q); }
        catch(SQLException e){ throw new IllegalStateException("No se pudo inicializar los módulos avanzados",e); }
    }

    public static List<Map<String,Object>> query(String sql,Object... args){
        List<Map<String,Object>> rows=new ArrayList<>();
        try(Connection c=Database.conectar(); PreparedStatement ps=c.prepareStatement(sql)){bind(ps,args);try(ResultSet rs=ps.executeQuery()){var md=rs.getMetaData();int n=md.getColumnCount();while(rs.next()){Map<String,Object> r=new LinkedHashMap<>();for(int i=1;i<=n;i++)r.put(md.getColumnLabel(i),rs.getObject(i));rows.add(r);}}}
        catch(SQLException e){throw new IllegalStateException("Error consultando módulos avanzados",e);} return rows;
    }
    public static long execute(String sql,Object... args){
        try(Connection c=Database.conectar(); PreparedStatement ps=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){bind(ps,args);ps.executeUpdate();try(ResultSet rs=ps.getGeneratedKeys()){return rs.next()?rs.getLong(1):0;}}
        catch(SQLException e){throw new IllegalStateException("Error modificando módulos avanzados",e);}
    }
    private static void bind(PreparedStatement ps,Object[] args)throws SQLException{for(int i=0;i<args.length;i++){if(args[i]==null)ps.setNull(i+1,Types.NULL);else ps.setObject(i+1,args[i]);}}
    public static Object scalar(String sql,Object...args){var r=query(sql,args);return r.isEmpty()?0:r.get(0).values().iterator().next();}
    public static String now(){return LocalDateTime.now().toString();}
}
