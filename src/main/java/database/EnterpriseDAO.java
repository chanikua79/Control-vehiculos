package database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/** Persistencia de las funciones empresariales adicionales del sistema. */
public final class EnterpriseDAO {
    private EnterpriseDAO() {}

    public static void initialize() {
        InicializadorBD.crearTablas();
        String[] sql = {
            "CREATE TABLE IF NOT EXISTS usuarios (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password_hash TEXT NOT NULL, nombre TEXT NOT NULL, rol TEXT NOT NULL DEFAULT 'EMPLEADO', activo INTEGER NOT NULL DEFAULT 1, creado_en TEXT NOT NULL)",
            "CREATE TABLE IF NOT EXISTS auditoria (id INTEGER PRIMARY KEY AUTOINCREMENT, usuario TEXT NOT NULL, accion TEXT NOT NULL, entidad TEXT, entidad_id INTEGER, fecha_hora TEXT NOT NULL, detalles TEXT)",
            "CREATE TABLE IF NOT EXISTS reservas (id INTEGER PRIMARY KEY AUTOINCREMENT, vehiculo_id INTEGER NOT NULL, empleado_id INTEGER NOT NULL, inicio TEXT NOT NULL, fin TEXT NOT NULL, motivo TEXT, estado TEXT NOT NULL DEFAULT 'PENDIENTE')",
            "CREATE TABLE IF NOT EXISTS mantenimientos (id INTEGER PRIMARY KEY AUTOINCREMENT, vehiculo_id INTEGER NOT NULL, tipo TEXT NOT NULL, descripcion TEXT, fecha TEXT NOT NULL, kilometraje INTEGER DEFAULT 0, costo REAL DEFAULT 0, estado TEXT NOT NULL DEFAULT 'PROGRAMADO')",
            "CREATE TABLE IF NOT EXISTS combustible (id INTEGER PRIMARY KEY AUTOINCREMENT, vehiculo_id INTEGER NOT NULL, fecha_hora TEXT NOT NULL, litros REAL NOT NULL, costo REAL DEFAULT 0, kilometraje INTEGER DEFAULT 0, empleado_id INTEGER, observaciones TEXT)",
            "CREATE TABLE IF NOT EXISTS fuentes_camara (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, url TEXT, ubicacion TEXT, activa INTEGER NOT NULL DEFAULT 1, tipo TEXT NOT NULL DEFAULT 'MANUAL')",
            "CREATE TABLE IF NOT EXISTS detecciones_camara (id INTEGER PRIMARY KEY AUTOINCREMENT, fuente_id INTEGER, matricula TEXT NOT NULL, confianza REAL, fecha_hora TEXT NOT NULL, tipo_evento TEXT NOT NULL DEFAULT 'DETECCION', procesado INTEGER NOT NULL DEFAULT 0, observaciones TEXT)",
            "CREATE TABLE IF NOT EXISTS alertas (id INTEGER PRIMARY KEY AUTOINCREMENT, tipo TEXT NOT NULL, mensaje TEXT NOT NULL, severidad TEXT NOT NULL DEFAULT 'INFO', fecha_hora TEXT NOT NULL, resuelta INTEGER NOT NULL DEFAULT 0)",
            "CREATE TABLE IF NOT EXISTS incidencias (id INTEGER PRIMARY KEY AUTOINCREMENT, vehiculo_id INTEGER NOT NULL, empleado_id INTEGER, tipo TEXT NOT NULL, descripcion TEXT, prioridad TEXT NOT NULL DEFAULT 'MEDIA', estado TEXT NOT NULL DEFAULT 'ABIERTA', foto_ref TEXT, creado_en TEXT NOT NULL, resuelto_en TEXT)",
            "CREATE TABLE IF NOT EXISTS documentos_vehiculo (id INTEGER PRIMARY KEY AUTOINCREMENT, vehiculo_id INTEGER NOT NULL, tipo TEXT NOT NULL, numero TEXT, fecha_emision TEXT, fecha_vencimiento TEXT, archivo_ref TEXT, estado TEXT NOT NULL DEFAULT 'VIGENTE')",
            "CREATE TABLE IF NOT EXISTS checklists (id INTEGER PRIMARY KEY AUTOINCREMENT, vehiculo_id INTEGER NOT NULL, empleado_id INTEGER, viaje_id INTEGER, fecha_hora TEXT NOT NULL, resultado TEXT NOT NULL, items_json TEXT NOT NULL, observaciones TEXT)",
            "CREATE TABLE IF NOT EXISTS control_acceso (id INTEGER PRIMARY KEY AUTOINCREMENT, vehiculo_id INTEGER, empleado_id INTEGER, matricula TEXT, metodo TEXT NOT NULL, direccion TEXT NOT NULL, fecha_hora TEXT NOT NULL, resultado TEXT NOT NULL, motivo TEXT, camara_id INTEGER)",
            "CREATE TABLE IF NOT EXISTS posiciones_gps (id INTEGER PRIMARY KEY AUTOINCREMENT, vehiculo_id INTEGER NOT NULL, fecha_hora TEXT NOT NULL, latitud REAL NOT NULL, longitud REAL NOT NULL, velocidad REAL DEFAULT 0, rumbo REAL DEFAULT 0, fuente TEXT DEFAULT 'GPS')",
            "CREATE TABLE IF NOT EXISTS empresas (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, identificador TEXT UNIQUE NOT NULL, activa INTEGER NOT NULL DEFAULT 1, creado_en TEXT NOT NULL)",
            "CREATE TABLE IF NOT EXISTS sedes (id INTEGER PRIMARY KEY AUTOINCREMENT, empresa_id INTEGER NOT NULL, nombre TEXT NOT NULL, direccion TEXT, activa INTEGER NOT NULL DEFAULT 1)",
            "CREATE TABLE IF NOT EXISTS integraciones_webhook (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL, url TEXT NOT NULL, secreto TEXT, eventos TEXT NOT NULL, activa INTEGER NOT NULL DEFAULT 1)",
            "CREATE UNIQUE INDEX IF NOT EXISTS idx_asig_vehiculo_activa ON asignaciones(vehiculo_id) WHERE activa=1",
            "CREATE UNIQUE INDEX IF NOT EXISTS idx_asig_empleado_activa ON asignaciones(empleado_id) WHERE activa=1",
            "CREATE INDEX IF NOT EXISTS idx_auditoria_fecha ON auditoria(fecha_hora)",
            "CREATE INDEX IF NOT EXISTS idx_reservas_vehiculo ON reservas(vehiculo_id, inicio, fin)",
            "CREATE INDEX IF NOT EXISTS idx_mantenimiento_vehiculo ON mantenimientos(vehiculo_id, fecha)",
            "CREATE INDEX IF NOT EXISTS idx_combustible_vehiculo ON combustible(vehiculo_id, fecha_hora)",
            "CREATE INDEX IF NOT EXISTS idx_detecciones_fecha ON detecciones_camara(fecha_hora)",
            "CREATE INDEX IF NOT EXISTS idx_alertas_resueltas ON alertas(resuelta, fecha_hora)",
            "CREATE INDEX IF NOT EXISTS idx_incidencias_estado ON incidencias(estado, prioridad, creado_en)",
            "CREATE INDEX IF NOT EXISTS idx_documentos_vencimiento ON documentos_vehiculo(fecha_vencimiento, estado)",
            "CREATE INDEX IF NOT EXISTS idx_acceso_fecha ON control_acceso(fecha_hora)",
            "CREATE INDEX IF NOT EXISTS idx_gps_vehiculo_fecha ON posiciones_gps(vehiculo_id, fecha_hora)",
            "CREATE INDEX IF NOT EXISTS idx_sedes_empresa ON sedes(empresa_id)"
        };
        try (Connection c=Database.conectar(); Statement st=c.createStatement()) {
            for (String q: sql) st.executeUpdate(q);
            ensureColumn(c,"vehiculos","estado","TEXT NOT NULL DEFAULT 'DISPONIBLE'");
            ensureColumn(c,"vehiculos","kilometraje","INTEGER NOT NULL DEFAULT 0");
            ensureColumn(c,"vehiculos","combustible_porcentaje","INTEGER NOT NULL DEFAULT 100");
        } catch(SQLException e) { throw new IllegalStateException("No se pudo inicializar la BD empresarial",e); }
    }

    private static void ensureColumn(Connection c,String table,String column,String definition) throws SQLException {
        boolean exists=false;
        try(ResultSet rs=c.getMetaData().getColumns(null,null,table,column)) { exists=rs.next(); }
        if(!exists) try(Statement st=c.createStatement()) { st.executeUpdate("ALTER TABLE "+table+" ADD COLUMN "+column+" "+definition); }
    }

    public static List<Map<String,Object>> query(String sql,Object... args) {
        List<Map<String,Object>> rows=new ArrayList<>();
        try(Connection c=Database.conectar(); PreparedStatement ps=c.prepareStatement(sql)) {
            bind(ps,args); try(ResultSet rs=ps.executeQuery()) { ResultSetMetaData md=rs.getMetaData(); int n=md.getColumnCount(); while(rs.next()){ Map<String,Object> row=new LinkedHashMap<>(); for(int i=1;i<=n;i++) row.put(md.getColumnLabel(i),rs.getObject(i)); rows.add(row); } }
        } catch(SQLException e){ throw new IllegalStateException("Error consultando BD",e); }
        return rows;
    }

    public static long execute(String sql,Object... args) {
        try(Connection c=Database.conectar(); PreparedStatement ps=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            bind(ps,args); ps.executeUpdate(); try(ResultSet rs=ps.getGeneratedKeys()){ return rs.next()?rs.getLong(1):0; }
        } catch(SQLException e){ throw new IllegalStateException("Error modificando BD",e); }
    }

    private static void bind(PreparedStatement ps,Object[] args) throws SQLException { for(int i=0;i<args.length;i++){ Object a=args[i]; if(a==null) ps.setNull(i+1,Types.NULL); else ps.setObject(i+1,a); } }

    public static List<Map<String,Object>> dashboard() {
        Map<String,Object> r=new LinkedHashMap<>();
        r.put("vehiculos", scalar("SELECT COUNT(*) FROM vehiculos"));
        r.put("vehiculosDisponibles", scalar("SELECT COUNT(*) FROM vehiculos WHERE estado='DISPONIBLE' AND activo=1"));
        r.put("vehiculosEnViaje", scalar("SELECT COUNT(*) FROM viajes WHERE entrada IS NULL"));
        r.put("vehiculosMantenimiento", scalar("SELECT COUNT(*) FROM vehiculos WHERE estado='MANTENIMIENTO'"));
        r.put("empleados", scalar("SELECT COUNT(*) FROM empleados"));
        r.put("asignacionesActivas", scalar("SELECT COUNT(*) FROM asignaciones WHERE activa=1"));
        r.put("viajesHoy", scalar("SELECT COUNT(*) FROM viajes WHERE date(salida)=date('now','localtime')"));
        r.put("alertasPendientes", scalar("SELECT COUNT(*) FROM alertas WHERE resuelta=0"));
        r.put("incidenciasAbiertas", scalar("SELECT COUNT(*) FROM incidencias WHERE estado NOT IN ('RESUELTA','CERRADA')"));
        r.put("accesosRechazadosHoy", scalar("SELECT COUNT(*) FROM control_acceso WHERE resultado='RECHAZADO' AND date(fecha_hora)=date('now','localtime')"));
        r.put("mantenimientoCostos", scalar("SELECT COALESCE(SUM(costo),0) FROM mantenimientos WHERE strftime('%Y-%m',fecha)=strftime('%Y-%m','now','localtime')"));
        r.put("combustibleLitrosMes", scalar("SELECT COALESCE(SUM(litros),0) FROM combustible WHERE strftime('%Y-%m',fecha_hora)=strftime('%Y-%m','now','localtime')"));
        r.put("documentosPorVencer", scalar("SELECT COUNT(*) FROM documentos_vehiculo WHERE fecha_vencimiento IS NOT NULL AND date(fecha_vencimiento)<=date('now','localtime','+30 day') AND estado<>'VENCIDO'"));
        return List.of(r);
    }

    public static Object scalar(String sql){ List<Map<String,Object>> r=query(sql); return r.isEmpty()?0:r.get(0).values().iterator().next(); }

    public static void seedAdmin(String hash){
        if(((Number)scalar("SELECT COUNT(*) FROM usuarios")).longValue()==0)
            execute("INSERT INTO usuarios(username,password_hash,nombre,rol,activo,creado_en) VALUES(?,?,?,?,1,?)","admin",hash,"Administrador","ADMIN",LocalDateTime.now().toString());
    }
}
