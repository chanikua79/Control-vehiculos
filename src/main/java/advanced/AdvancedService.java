package advanced;

import database.EnterpriseDAO;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.*;
import java.util.*;

@Service
public class AdvancedService {
    private final SecureRandom random=new SecureRandom();
    public AdvancedService(){AdvancedDAO.initialize();}

    public List<Map<String,Object>> keys(){return AdvancedDAO.query("SELECT k.*,v.matricula FROM llaves_vehiculo k JOIN vehiculos v ON v.id=k.vehiculo_id ORDER BY k.id DESC");}
    public long createKey(long vehicle,String code){return AdvancedDAO.execute("INSERT INTO llaves_vehiculo(vehiculo_id,codigo) VALUES(?,?)",vehicle,code);}
    public void deliverKey(long id,long employee,String notes){var now=AdvancedDAO.now();AdvancedDAO.execute("UPDATE llaves_vehiculo SET estado='ENTREGADA',entregada_a=?,entregada_en=?,observaciones=? WHERE id=?",employee,now,notes,id);}
    public void returnKey(long id,String notes){AdvancedDAO.execute("UPDATE llaves_vehiculo SET estado='DISPONIBLE',devuelta_en=?,observaciones=? WHERE id=?",AdvancedDAO.now(),notes,id);}

    public List<Map<String,Object>> inspections(){return AdvancedDAO.query("SELECT i.*,v.matricula,e.nombre empleado_nombre FROM inspecciones_danos i JOIN vehiculos v ON v.id=i.vehiculo_id LEFT JOIN empleados e ON e.id=i.empleado_id ORDER BY i.id DESC");}
    public long inspection(long vehicle,Long employee,Long trip,String type,String obs,String photos,String result){return AdvancedDAO.execute("INSERT INTO inspecciones_danos(vehiculo_id,empleado_id,viaje_id,momento,tipo,observaciones,fotos_json,resultado) VALUES(?,?,?,?,?,?,?,?)",vehicle,employee,trip,AdvancedDAO.now(),type,obs,photos,result);}
    public List<Map<String,Object>> checkGeofences(long vehicle,double lat,double lon){
        List<Map<String,Object>> result=new ArrayList<>();
        for(var g:geofences()){
            double glat=((Number)g.get("latitud")).doubleValue(), glon=((Number)g.get("longitud")).doubleValue(), radius=((Number)g.get("radio_metros")).doubleValue();
            double distance=distanceMeters(lat,lon,glat,glon); boolean inside=distance<=radius;
            Map<String,Object> row=new LinkedHashMap<>(); row.put("geocerca",g.get("nombre")); row.put("distanciaMetros",Math.round(distance*100.0)/100.0); row.put("dentro",inside); result.add(row);
            if(!inside && ((Number)g.get("activa")).intValue()==1){String plate=String.valueOf(EnterpriseDAO.query("SELECT matricula FROM vehiculos WHERE id=?",vehicle).stream().findFirst().map(x->x.get("matricula")).orElse(vehicle));serviceAlert("GEOCERCA_SALIDA","El vehículo "+plate+" salió de "+g.get("nombre"),"AVISO");}
        }
        return result;
    }
    private void serviceAlert(String type,String message,String severity){EnterpriseDAO.execute("INSERT INTO alertas(tipo,mensaje,severidad,fecha_hora,resuelta) SELECT ?,?,?,datetime('now','localtime'),0 WHERE NOT EXISTS (SELECT 1 FROM alertas WHERE tipo=? AND mensaje=? AND resuelta=0)",type,message,severity,type,message);}
    private static double distanceMeters(double a,double b,double c,double d){double R=6371000, p1=Math.toRadians(a),p2=Math.toRadians(c),dp=Math.toRadians(c-a),dl=Math.toRadians(d-b);double x=Math.sin(dp/2)*Math.sin(dp/2)+Math.cos(p1)*Math.cos(p2)*Math.sin(dl/2)*Math.sin(dl/2);return 2*R*Math.atan2(Math.sqrt(x),Math.sqrt(1-x));}
    public List<Map<String,Object>> geofences(){return AdvancedDAO.query("SELECT * FROM geocercas ORDER BY id");}
    public long geofence(String name,double lat,double lon,double radius){if(lat<-90||lat>90||lon<-180||lon>180||radius<=0)throw new IllegalArgumentException("Geocerca invalida");return AdvancedDAO.execute("INSERT INTO geocercas(nombre,latitud,longitud,radio_metros) VALUES(?,?,?,?)",name,lat,lon,radius);}
    public void geofenceState(long id,boolean active){AdvancedDAO.execute("UPDATE geocercas SET activa=? WHERE id=?",active?1:0,id);}

    public List<Map<String,Object>> rules(){return AdvancedDAO.query("SELECT * FROM reglas_flotas ORDER BY id");}
    public long rule(String name,String event,String condition,String action,String severity){return AdvancedDAO.execute("INSERT INTO reglas_flotas(nombre,evento,condicion,accion,severidad) VALUES(?,?,?,?,?)",name,event,condition,action,severity);}
    public void ruleState(long id,boolean active){AdvancedDAO.execute("UPDATE reglas_flotas SET activa=? WHERE id=?",active?1:0,id);}
    public List<Map<String,Object>> schedules(){return AdvancedDAO.query("SELECT * FROM horarios_operacion ORDER BY dia_semana,inicio");}
    public long schedule(String name,int day,String start,String end,boolean approval){if(day<1||day>7)throw new IllegalArgumentException("Dia invalido");return AdvancedDAO.execute("INSERT INTO horarios_operacion(nombre,dia_semana,inicio,fin,requiere_autorizacion) VALUES(?,?,?,?,?)",name,day,start,end,approval?1:0);}
    public long holiday(String date,String name,boolean approval){return AdvancedDAO.execute("INSERT INTO feriados(fecha,nombre,requiere_autorizacion) VALUES(?,?,?)",date,name,approval?1:0);}
    public List<Map<String,Object>> holidays(){return AdvancedDAO.query("SELECT * FROM feriados ORDER BY fecha");}

    public List<Map<String,Object>> approvals(){return AdvancedDAO.query("SELECT * FROM aprobaciones ORDER BY id DESC");}
    public long approval(String type,long ref,String requester){return AdvancedDAO.execute("INSERT INTO aprobaciones(tipo,referencia_id,solicitante,creado_en) VALUES(?,?,?,?)",type,ref,requester,AdvancedDAO.now());}
    public void approvalState(long id,String state,String approver,String comment){AdvancedDAO.execute("UPDATE aprobaciones SET estado=?,aprobador=?,comentario=?,resuelto_en=? WHERE id=?",state,approver,comment,AdvancedDAO.now(),id);}
    public List<Map<String,Object>> notifications(String user){return AdvancedDAO.query("SELECT * FROM notificaciones WHERE usuario IS NULL OR usuario=? ORDER BY id DESC LIMIT 200",user);}
    public long notify(String user,String title,String message,String severity){return AdvancedDAO.execute("INSERT INTO notificaciones(usuario,titulo,mensaje,severidad,creado_en) VALUES(?,?,?,?,?)",user,title,message,severity,AdvancedDAO.now());}
    public void markRead(long id){AdvancedDAO.execute("UPDATE notificaciones SET leida=1 WHERE id=?",id);}

    public List<Map<String,Object>> photos(){return AdvancedDAO.query("SELECT * FROM fotos_evento ORDER BY id DESC");}
    public long photo(String entity,long entityId,String type,String ref){return AdvancedDAO.execute("INSERT INTO fotos_evento(entidad,entidad_id,tipo,referencia,creado_en) VALUES(?,?,?,?,?)",entity,entityId,type,ref,AdvancedDAO.now());}
    public List<Map<String,Object>> devices(){return AdvancedDAO.query("SELECT * FROM dispositivos ORDER BY id");}
    public long device(String name,String type,String identifier,String location){return AdvancedDAO.execute("INSERT INTO dispositivos(nombre,tipo,identificador,ubicacion) VALUES(?,?,?,?)",name,type,identifier,location);}
    public void deviceHeartbeat(long id){AdvancedDAO.execute("UPDATE dispositivos SET ultimo_contacto=?,activa=1 WHERE id=?",AdvancedDAO.now(),id);}

    public List<Map<String,Object>> modules(){return AdvancedDAO.query("SELECT * FROM configuracion_modulos ORDER BY modulo");}
    public void module(String name,boolean enabled,String config){AdvancedDAO.execute("INSERT INTO configuracion_modulos(modulo,habilitado,configuracion_json) VALUES(?,?,?) ON CONFLICT(modulo) DO UPDATE SET habilitado=excluded.habilitado,configuracion_json=excluded.configuracion_json",name,enabled?1:0,config==null?"{}":config);}
    public List<Map<String,Object>> subscriptions(){return AdvancedDAO.query("SELECT s.*,e.nombre empresa_nombre FROM suscripciones s JOIN empresas e ON e.id=s.empresa_id ORDER BY s.id DESC");}
    public long subscription(long company,String plan,String state,String start,String end,int vehicles,int users){return AdvancedDAO.execute("INSERT INTO suscripciones(empresa_id,plan,estado,inicio,fin,limite_vehiculos,limite_usuarios) VALUES(?,?,?,?,?,?,?)",company,plan,state,start,end,vehicles,users);}
    public List<Map<String,Object>> driverProfiles(){return AdvancedDAO.query("SELECT p.*,e.nombre FROM perfiles_conductor p JOIN empleados e ON e.id=p.empleado_id ORDER BY e.nombre");}
    public void driverProfile(long employee,String license,String category,String expiry,String restrictions,String phone,String email){AdvancedDAO.execute("INSERT INTO perfiles_conductor(empleado_id,licencia_numero,categoria,licencia_vencimiento,restricciones,telefono,correo) VALUES(?,?,?,?,?,?,?) ON CONFLICT(empleado_id) DO UPDATE SET licencia_numero=excluded.licencia_numero,categoria=excluded.categoria,licencia_vencimiento=excluded.licencia_vencimiento,restricciones=excluded.restricciones,telefono=excluded.telefono,correo=excluded.correo",employee,license,category,expiry,restrictions,phone,email);}
    public List<Map<String,Object>> routes(long vehicle){return AdvancedDAO.query("SELECT * FROM rutas_gps WHERE vehiculo_id=? ORDER BY id DESC",vehicle);}
    public long route(long vehicle,String name,String start,String end,double distance,int minutes,String points){return AdvancedDAO.execute("INSERT INTO rutas_gps(vehiculo_id,nombre,inicio,fin,distancia_km,duracion_minutos,puntos_json) VALUES(?,?,?,?,?,?,?)",vehicle,name,start,end,distance,minutes,points);}
    public List<Map<String,Object>> apiKeys(){return AdvancedDAO.query("SELECT id,nombre,permisos,expira_en,activa,creado_en FROM api_keys ORDER BY id DESC");}
    public Map<String,Object> createApiKey(String name,String permissions,String expiry){byte[] b=new byte[36];random.nextBytes(b);String raw="cv_"+Base64.getUrlEncoder().withoutPadding().encodeToString(b);AdvancedDAO.execute("INSERT INTO api_keys(nombre,clave_hash,permisos,expira_en,creado_en) VALUES(?,?,?,?,?)",name,sha256(raw),permissions,expiry,AdvancedDAO.now());return Map.of("clave",raw,"advertencia","Guarde esta clave; no se vuelve a mostrar.");}
    public void apiKeyState(long id,boolean active){AdvancedDAO.execute("UPDATE api_keys SET activa=? WHERE id=?",active?1:0,id);}
    public List<Map<String,Object>> telemetry(long vehicle){return AdvancedDAO.query("SELECT * FROM telemetria_eventos WHERE vehiculo_id=? ORDER BY id DESC LIMIT 500",vehicle);}
    public long telemetry(Long vehicle,String type,double value,String unit,String origin){return AdvancedDAO.execute("INSERT INTO telemetria_eventos(vehiculo_id,tipo,valor,unidad,fecha_hora,origen) VALUES(?,?,?,?,?,?)",vehicle,type,value,unit,AdvancedDAO.now(),origin);}

    public Map<String,Object> temporaryQr(String subject,int seconds){if(seconds<5||seconds>3600)throw new IllegalArgumentException("Duracion invalida");byte[] b=new byte[32];random.nextBytes(b);String raw=Base64.getUrlEncoder().withoutPadding().encodeToString(b);String hash=sha256(raw);String exp=LocalDateTime.now().plusSeconds(seconds).toString();AdvancedDAO.execute("INSERT INTO tokens_temporales(sujeto,token_hash,expira_en) VALUES(?,?,?)",subject,hash,exp);return Map.of("token",raw,"sujeto",subject,"expiraEn",exp);}
    public Map<String,Object> consumeQr(String token){if(token==null||token.isBlank())return Map.of("valido",false,"motivo","TOKEN_VACIO");var rows=AdvancedDAO.query("SELECT * FROM tokens_temporales WHERE token_hash=? AND usado=0 AND datetime(expira_en)>datetime('now','localtime')",sha256(token));if(rows.isEmpty())return Map.of("valido",false,"motivo","TOKEN_INVALIDO_O_EXPIRADO");long id=((Number)rows.get(0).get("id")).longValue();AdvancedDAO.execute("UPDATE tokens_temporales SET usado=1 WHERE id=?",id);return Map.of("valido",true,"sujeto",rows.get(0).get("sujeto"));}
    private static String sha256(String s){try{byte[] d=MessageDigest.getInstance("SHA-256").digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));StringBuilder x=new StringBuilder();for(byte v:d)x.append(String.format("%02x",v));return x.toString();}catch(Exception e){throw new IllegalStateException(e);}}

    public void alert(String type,String message,String severity){
        EnterpriseDAO.execute(
            "INSERT INTO alertas(tipo,mensaje,severidad,fecha_hora,resuelta) " +
            "SELECT ?,?,?,datetime('now','localtime'),0 " +
            "WHERE NOT EXISTS (" +
            "SELECT 1 FROM alertas WHERE tipo=? AND mensaje=? AND resuelta=0)",
            type,message,severity,type,message
        );
    }

    public Map<String,Object> assistant(String question){String q=question==null?"":question.toLowerCase(Locale.ROOT);Map<String,Object> out=new LinkedHashMap<>();out.put("consulta",question);if(q.contains("cuántos")&&q.contains("vehículo")){out.put("respuesta","Hay "+EnterpriseDAO.scalar("SELECT COUNT(*) FROM vehiculos")+" vehículos registrados.");}else if(q.contains("fuera")||q.contains("viaje")){out.put("respuesta","Hay "+EnterpriseDAO.scalar("SELECT COUNT(*) FROM viajes WHERE entrada IS NULL")+" vehículos actualmente en viaje.");}else if(q.contains("alerta")){out.put("respuesta","Hay "+EnterpriseDAO.scalar("SELECT COUNT(*) FROM alertas WHERE resuelta=0")+" alertas pendientes.");}else if(q.contains("mantenimiento")){out.put("respuesta","Hay "+EnterpriseDAO.scalar("SELECT COUNT(*) FROM mantenimientos WHERE estado='PROGRAMADO'")+" mantenimientos programados.");}else if(q.contains("rechaz")){out.put("respuesta","Hay "+EnterpriseDAO.scalar("SELECT COUNT(*) FROM control_acceso WHERE resultado='RECHAZADO' AND date(fecha_hora)=date('now','localtime')")+" accesos rechazados hoy.");}else{out.put("respuesta","Puedo consultar vehículos, viajes, alertas, mantenimiento y accesos. La consulta no ejecuta cambios.");}return out;}
}
