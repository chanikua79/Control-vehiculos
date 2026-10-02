package service;

import database.EnterpriseDAO;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class EnterpriseService {
    public EnterpriseService(){ EnterpriseDAO.initialize(); }
    public List<Map<String,Object>> dashboard(){ return EnterpriseDAO.dashboard(); }
    public void audit(String user,String action,String entity,Integer id,String details){ EnterpriseDAO.execute("INSERT INTO auditoria(usuario,accion,entidad,entidad_id,fecha_hora,detalles) VALUES(?,?,?,?,?,?)",user,action,entity,id,LocalDateTime.now().toString(),details); }
    public List<Map<String,Object>> audit(int limit){ return EnterpriseDAO.query("SELECT * FROM auditoria ORDER BY id DESC LIMIT ?",limit); }
    public List<Map<String,Object>> users(){ return EnterpriseDAO.query("SELECT id,username,nombre,rol,activo,creado_en FROM usuarios ORDER BY id"); }
    public void createUser(String username,String hash,String nombre,String rol){ EnterpriseDAO.execute("INSERT INTO usuarios(username,password_hash,nombre,rol,activo,creado_en) VALUES(?,?,?,?,1,?)",username,hash,nombre,rol,LocalDateTime.now().toString()); }
    public void setUserActive(long id,boolean active){ EnterpriseDAO.execute("UPDATE usuarios SET activo=? WHERE id=?",active?1:0,id); }
    public List<Map<String,Object>> vehicles(){ return EnterpriseDAO.query("SELECT * FROM vehiculos ORDER BY id"); }
    public void setVehicleState(long id,String state){ EnterpriseDAO.execute("UPDATE vehiculos SET estado=? WHERE id=?",state,id); }
    public void updateVehicleTelemetry(long id,int km,int fuel){ if(km<0||fuel<0||fuel>100) throw new IllegalArgumentException("Kilometraje o combustible invalido"); EnterpriseDAO.execute("UPDATE vehiculos SET kilometraje=?, combustible_porcentaje=? WHERE id=?",km,fuel,id); }
    public List<Map<String,Object>> reservations(){ return EnterpriseDAO.query("SELECT r.*,v.matricula,e.nombre empleado_nombre FROM reservas r JOIN vehiculos v ON v.id=r.vehiculo_id JOIN empleados e ON e.id=r.empleado_id ORDER BY r.inicio DESC"); }
    public long createReservation(long vehicle,long employee,String start,String end,String reason){
        long conflicts=((Number)EnterpriseDAO.query("SELECT COUNT(*) c FROM reservas WHERE vehiculo_id=? AND estado<>'CANCELADA' AND inicio < ? AND fin > ?",vehicle,end,start).get(0).get("c")).longValue();
        if(conflicts>0) throw new IllegalArgumentException("El vehiculo ya tiene una reserva en ese horario");
        return EnterpriseDAO.execute("INSERT INTO reservas(vehiculo_id,empleado_id,inicio,fin,motivo,estado) VALUES(?,?,?,?,?,'PENDIENTE')",vehicle,employee,start,end,reason); }
    public void reservationState(long id,String state){ EnterpriseDAO.execute("UPDATE reservas SET estado=? WHERE id=?",state,id); }
    public List<Map<String,Object>> maintenance(){ return EnterpriseDAO.query("SELECT m.*,v.matricula FROM mantenimientos m JOIN vehiculos v ON v.id=m.vehiculo_id ORDER BY m.fecha DESC"); }
    public long createMaintenance(long vehicle,String type,String description,String date,int km,double cost,String state){ return EnterpriseDAO.execute("INSERT INTO mantenimientos(vehiculo_id,tipo,descripcion,fecha,kilometraje,costo,estado) VALUES(?,?,?,?,?,?,?)",vehicle,type,description,date,km,cost,state); }
    public void maintenanceState(long id,String state){ EnterpriseDAO.execute("UPDATE mantenimientos SET estado=? WHERE id=?",state,id); }
    public List<Map<String,Object>> fuel(){ return EnterpriseDAO.query("SELECT f.*,v.matricula,e.nombre empleado_nombre FROM combustible f JOIN vehiculos v ON v.id=f.vehiculo_id LEFT JOIN empleados e ON e.id=f.empleado_id ORDER BY f.fecha_hora DESC"); }
    public long addFuel(long vehicle,double liters,double cost,int km,Long employee,String notes){ if(liters<=0||cost<0||km<0) throw new IllegalArgumentException("Datos de combustible invalidos"); return EnterpriseDAO.execute("INSERT INTO combustible(vehiculo_id,fecha_hora,litros,costo,kilometraje,empleado_id,observaciones) VALUES(?,?,?,?,?,?,?)",vehicle,LocalDateTime.now().toString(),liters,cost,km,employee,notes); }
    public List<Map<String,Object>> cameraSources(){ return EnterpriseDAO.query("SELECT * FROM fuentes_camara ORDER BY id"); }
    public long addCameraSource(String name,String url,String location,String type){ return EnterpriseDAO.execute("INSERT INTO fuentes_camara(nombre,url,ubicacion,tipo) VALUES(?,?,?,?)",name,url,location,type); }
    public void cameraSourceState(long id,boolean active){ EnterpriseDAO.execute("UPDATE fuentes_camara SET activa=? WHERE id=?",active?1:0,id); }
    public List<Map<String,Object>> detections(int limit){ return EnterpriseDAO.query("SELECT d.*,f.nombre fuente FROM detecciones_camara d LEFT JOIN fuentes_camara f ON f.id=d.fuente_id ORDER BY d.id DESC LIMIT ?",limit); }
    public long detection(Long source,String plate,Double confidence,String event,String notes){ return EnterpriseDAO.execute("INSERT INTO detecciones_camara(fuente_id,matricula,confianza,fecha_hora,tipo_evento,procesado,observaciones) VALUES(?,?,?,?,?,0,?)",source,plate,confidence,LocalDateTime.now().toString(),event,notes); }
    public List<Map<String,Object>> alerts(boolean unresolved){ return EnterpriseDAO.query("SELECT * FROM alertas WHERE resuelta=? ORDER BY id DESC",unresolved?0:1); }
    public long alert(String type,String message,String severity){ return EnterpriseDAO.execute("INSERT INTO alertas(tipo,mensaje,severidad,fecha_hora,resuelta) VALUES(?,?,?,?,0)",type,message,severity,LocalDateTime.now().toString()); }
    public void resolveAlert(long id){ EnterpriseDAO.execute("UPDATE alertas SET resuelta=1 WHERE id=?",id); }
    public List<Map<String,Object>> incidencias(){ return EnterpriseDAO.query("SELECT i.*,v.matricula,e.nombre empleado_nombre FROM incidencias i JOIN vehiculos v ON v.id=i.vehiculo_id LEFT JOIN empleados e ON e.id=i.empleado_id ORDER BY i.id DESC"); }
    public long createIncident(long vehicle,Long employee,String type,String description,String priority,String photo){ return EnterpriseDAO.execute("INSERT INTO incidencias(vehiculo_id,empleado_id,tipo,descripcion,prioridad,estado,foto_ref,creado_en) VALUES(?,?,?,?,?,'ABIERTA',?,?)",vehicle,employee,type,description,priority,photo,LocalDateTime.now().toString()); }
    public void incidentState(long id,String state){ EnterpriseDAO.execute("UPDATE incidencias SET estado=?,resuelto_en=CASE WHEN ? IN ('RESUELTA','CERRADA') THEN ? ELSE resuelto_en END WHERE id=?",state,state,LocalDateTime.now().toString(),id); }
    public List<Map<String,Object>> documents(){ return EnterpriseDAO.query("SELECT d.*,v.matricula FROM documentos_vehiculo d JOIN vehiculos v ON v.id=d.vehiculo_id ORDER BY COALESCE(d.fecha_vencimiento,'9999-12-31')"); }
    public long addDocument(long vehicle,String type,String number,String issued,String expiry,String fileRef){ return EnterpriseDAO.execute("INSERT INTO documentos_vehiculo(vehiculo_id,tipo,numero,fecha_emision,fecha_vencimiento,archivo_ref,estado) VALUES(?,?,?,?,?,?,CASE WHEN ? IS NOT NULL AND date(?)<date('now','localtime') THEN 'VENCIDO' ELSE 'VIGENTE' END)",vehicle,type,number,issued,expiry,fileRef,expiry,expiry); }
    public void documentState(long id,String state){ EnterpriseDAO.execute("UPDATE documentos_vehiculo SET estado=? WHERE id=?",state,id); }
    public List<Map<String,Object>> checklists(){ return EnterpriseDAO.query("SELECT c.*,v.matricula,e.nombre empleado_nombre FROM checklists c JOIN vehiculos v ON v.id=c.vehiculo_id LEFT JOIN empleados e ON e.id=c.empleado_id ORDER BY c.id DESC"); }
    public long addChecklist(long vehicle,Long employee,Long trip,String result,String items,String notes){ return EnterpriseDAO.execute("INSERT INTO checklists(vehiculo_id,empleado_id,viaje_id,fecha_hora,resultado,items_json,observaciones) VALUES(?,?,?,?,?,?,?)",vehicle,employee,trip,LocalDateTime.now().toString(),result,items,notes); }
    public List<Map<String,Object>> accessEvents(int limit){ return EnterpriseDAO.query("SELECT a.*,v.matricula vehiculo_matricula,e.nombre empleado_nombre FROM control_acceso a LEFT JOIN vehiculos v ON v.id=a.vehiculo_id LEFT JOIN empleados e ON e.id=a.empleado_id ORDER BY a.id DESC LIMIT ?",limit); }
    public long accessEvent(Long vehicle,Long employee,String plate,String method,String direction,String result,String reason,Long camera){ return EnterpriseDAO.execute("INSERT INTO control_acceso(vehiculo_id,empleado_id,matricula,metodo,direccion,fecha_hora,resultado,motivo,camara_id) VALUES(?,?,?,?,?,?,?,?,?)",vehicle,employee,plate,method,direction,LocalDateTime.now().toString(),result,reason,camera); }
    public List<Map<String,Object>> gps(long vehicle,int limit){ return EnterpriseDAO.query("SELECT * FROM posiciones_gps WHERE vehiculo_id=? ORDER BY id DESC LIMIT ?",vehicle,limit); }
    public long gpsPosition(long vehicle,double lat,double lon,double speed,double heading,String source){ if(lat<-90||lat>90||lon<-180||lon>180) throw new IllegalArgumentException("Coordenadas invalidas"); return EnterpriseDAO.execute("INSERT INTO posiciones_gps(vehiculo_id,fecha_hora,latitud,longitud,velocidad,rumbo,fuente) VALUES(?,?,?,?,?,?,?)",vehicle,LocalDateTime.now().toString(),lat,lon,speed,heading,source); }
    public List<Map<String,Object>> companies(){ return EnterpriseDAO.query("SELECT * FROM empresas ORDER BY id"); }
    public long createCompany(String name,String identifier){ return EnterpriseDAO.execute("INSERT INTO empresas(nombre,identificador,creado_en) VALUES(?,?,?)",name,identifier,LocalDateTime.now().toString()); }
    public List<Map<String,Object>> sites(){ return EnterpriseDAO.query("SELECT s.*,e.nombre empresa_nombre FROM sedes s JOIN empresas e ON e.id=s.empresa_id ORDER BY s.id"); }
    public long createSite(long company,String name,String address){ return EnterpriseDAO.execute("INSERT INTO sedes(empresa_id,nombre,direccion) VALUES(?,?,?)",company,name,address); }
    public List<Map<String,Object>> webhooks(){ return EnterpriseDAO.query("SELECT id,nombre,url,eventos,activa FROM integraciones_webhook ORDER BY id"); }
    public long createWebhook(String name,String url,String secret,String events){ return EnterpriseDAO.execute("INSERT INTO integraciones_webhook(nombre,url,secreto,eventos) VALUES(?,?,?,?)",name,url,secret,events); }
    public List<Map<String,Object>> analytics(){ return EnterpriseDAO.query("SELECT v.id,v.matricula,(SELECT COUNT(*) FROM viajes t WHERE t.vehiculo_id=v.id) viajes,(SELECT COALESCE(SUM(t.duracion_minutos),0) FROM viajes t WHERE t.vehiculo_id=v.id) minutos,(SELECT COALESCE(SUM(c.litros),0) FROM combustible c WHERE c.vehiculo_id=v.id) litros,(SELECT COALESCE(SUM(m.costo),0) FROM mantenimientos m WHERE m.vehiculo_id=v.id) mantenimiento FROM vehiculos v ORDER BY viajes DESC"); }
    public Map<String,Object> processCameraEvent(String plate,String direction){
        var vehicles=EnterpriseDAO.query("SELECT id,activo FROM vehiculos WHERE upper(matricula)=upper(?)",plate);
        if(vehicles.isEmpty()){ long alert=alert("MATRICULA_DESCONOCIDA","Matrícula no registrada: "+plate,"ALTA"); return Map.of("resultado","RECHAZADO","motivo","MATRICULA_DESCONOCIDA","alerta",alert); }
        long vehicle=((Number)vehicles.get(0).get("id")).longValue();
        if(((Number)vehicles.get(0).get("activo")).intValue()!=1) return Map.of("resultado","RECHAZADO","motivo","VEHICULO_INACTIVO");
        if("SALIDA".equalsIgnoreCase(direction)){
            var a=EnterpriseDAO.query("SELECT id,empleado_id FROM asignaciones WHERE vehiculo_id=? AND activa=1",vehicle);
            if(a.isEmpty()){ long alert=alert("SALIDA_SIN_ASIGNACION","Salida rechazada para "+plate,"ALTA"); return Map.of("resultado","RECHAZADO","motivo","SIN_ASIGNACION","alerta",alert); }
            long emp=((Number)a.get(0).get("empleado_id")).longValue();
            var open=EnterpriseDAO.query("SELECT id FROM viajes WHERE vehiculo_id=? AND entrada IS NULL",vehicle);
            if(!open.isEmpty()) return Map.of("resultado","RECHAZADO","motivo","VIAJE_YA_ABIERTO");
            long trip=((Number)EnterpriseDAO.query("SELECT COALESCE(MAX(id),0)+1 n FROM viajes").get(0).get("n")).longValue();
            EnterpriseDAO.execute("INSERT INTO viajes(id,vehiculo_id,empleado_id,salida,entrada,duracion_minutos) VALUES(?,?,?,?,NULL,NULL)",trip,vehicle,emp,LocalDateTime.now().toString());
            EnterpriseDAO.execute("UPDATE vehiculos SET estado='EN_VIAJE' WHERE id=?",vehicle);
            return Map.of("resultado","AUTORIZADO","viajeId",trip,"empleadoId",emp);
        }
        if("ENTRADA".equalsIgnoreCase(direction)){
            var open=EnterpriseDAO.query("SELECT id,salida FROM viajes WHERE vehiculo_id=? AND entrada IS NULL ORDER BY id DESC LIMIT 1",vehicle);
            if(open.isEmpty()) return Map.of("resultado","RECHAZADO","motivo","SIN_VIAJE_ABIERTO");
            long trip=((Number)open.get(0).get("id")).longValue(); String exit=String.valueOf(open.get(0).get("salida")); String now=LocalDateTime.now().toString();
            long mins=Math.max(0,java.time.Duration.between(LocalDateTime.parse(exit),LocalDateTime.parse(now)).toMinutes());
            EnterpriseDAO.execute("UPDATE viajes SET entrada=?,duracion_minutos=? WHERE id=?",now,mins,trip);
            EnterpriseDAO.execute("UPDATE asignaciones SET activa=0,fin=? WHERE vehiculo_id=? AND activa=1",now,vehicle);
            EnterpriseDAO.execute("UPDATE vehiculos SET estado='DISPONIBLE' WHERE id=?",vehicle);
            return Map.of("resultado","AUTORIZADO","viajeId",trip,"duracionMinutos",mins);
        }
        return Map.of("resultado","REGISTRADO");
    }

    public List<Map<String,Object>> trips(){ return EnterpriseDAO.query("SELECT v.*,veh.matricula,e.nombre empleado_nombre FROM viajes v JOIN vehiculos veh ON veh.id=v.vehiculo_id JOIN empleados e ON e.id=v.empleado_id ORDER BY v.salida DESC"); }
    public List<Map<String,Object>> tripsByVehicle(long id){ return EnterpriseDAO.query("SELECT v.*,e.nombre empleado_nombre FROM viajes v JOIN empleados e ON e.id=v.empleado_id WHERE v.vehiculo_id=? ORDER BY v.salida DESC",id); }
}
