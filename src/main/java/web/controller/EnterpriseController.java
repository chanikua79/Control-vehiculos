package web.controller;

import database.EnterpriseDAO;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import service.EnterpriseService;
import camera.PlateDetection;
import camera.PlateRecognitionService;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.*;

@RestController
@RequestMapping("/api/enterprise")
public class EnterpriseController {
    private final EnterpriseService service; private final PasswordEncoder encoder; private final PlateRecognitionService recognition; private final service.RealtimeEventService realtime; private final service.BackupService backups;
    public EnterpriseController(EnterpriseService service,PasswordEncoder encoder,PlateRecognitionService recognition,service.RealtimeEventService realtime,service.BackupService backups){this.service=service;this.encoder=encoder;this.recognition=recognition;this.realtime=realtime;this.backups=backups;}
    private String s(Map<String,Object> b,String k){Object v=b.get(k); if(v==null||v.toString().isBlank())throw new IllegalArgumentException("El campo "+k+" es obligatorio"); return v.toString().trim();}
    private long l(Map<String,Object> b,String k){return Long.parseLong(s(b,k));}

    @GetMapping("/realtime") public SseEmitter realtime(){return realtime.subscribe();}
    @PostMapping("/backup") @PreAuthorize("hasRole('ADMIN')") public Map<String,Object> backup() throws Exception {return Map.of("archivo",backups.createBackup().toString(),"mensaje","Backup creado correctamente");}
    @GetMapping("/dashboard") public Map<String,Object> dashboard(){return service.dashboard().get(0);}
    @GetMapping("/vehiculos") public List<Map<String,Object>> vehicles(){return service.vehicles();}
    @PutMapping("/vehiculos/{id}/estado") @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')") public Map<String,Object> vehicleState(@PathVariable long id,@RequestBody Map<String,Object> b,Authentication a){String state=s(b,"estado").toUpperCase(); service.setVehicleState(id,state); service.audit(a.getName(),"CAMBIAR_ESTADO","VEHICULO",(int)id,state); return Map.of("mensaje","Estado actualizado","estado",state);}
    @PutMapping("/vehiculos/{id}/telemetria") public Map<String,Object> telemetry(@PathVariable long id,@RequestBody Map<String,Object> b,Authentication a){int km=((Number)b.get("kilometraje")).intValue();int fuel=((Number)b.get("combustiblePorcentaje")).intValue();service.updateVehicleTelemetry(id,km,fuel);service.audit(a.getName(),"ACTUALIZAR_TELEMETRIA","VEHICULO",(int)id,"km="+km+", combustible="+fuel);return Map.of("mensaje","Telemetria actualizada");}
    @GetMapping("/vehiculos/{id}/viajes") public List<Map<String,Object>> vehicleTrips(@PathVariable long id){return service.tripsByVehicle(id);}

    @GetMapping("/reservas") public List<Map<String,Object>> reservations(){return service.reservations();}
    @PostMapping("/reservas") @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','EMPLEADO')") public ResponseEntity<?> createReservation(@RequestBody Map<String,Object> b,Authentication a){long id=service.createReservation(l(b,"vehiculoId"),l(b,"empleadoId"),s(b,"inicio"),s(b,"fin"),String.valueOf(b.getOrDefault("motivo","")));service.audit(a.getName(),"CREAR_RESERVA","RESERVA",(int)id,safe(b.toString()));return ResponseEntity.status(201).body(Map.of("id",id,"mensaje","Reserva creada"));}
    @PutMapping("/reservas/{id}/estado") @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')") public Map<String,Object> reservationState(@PathVariable long id,@RequestBody Map<String,Object> b,Authentication a){String state=s(b,"estado").toUpperCase();service.reservationState(id,state);service.audit(a.getName(),"CAMBIAR_ESTADO","RESERVA",(int)id,state);return Map.of("mensaje","Reserva actualizada","estado",state);}

    @GetMapping("/mantenimiento") public List<Map<String,Object>> maintenance(){return service.maintenance();}
    @PostMapping("/mantenimiento") @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')") public ResponseEntity<?> maintenance(@RequestBody Map<String,Object> b,Authentication a){long id=service.createMaintenance(l(b,"vehiculoId"),s(b,"tipo"),String.valueOf(b.getOrDefault("descripcion","")),s(b,"fecha"),((Number)b.getOrDefault("kilometraje",0)).intValue(),((Number)b.getOrDefault("costo",0)).doubleValue(),String.valueOf(b.getOrDefault("estado","PROGRAMADO")));service.audit(a.getName(),"CREAR_MANTENIMIENTO","MANTENIMIENTO",(int)id,safe(b.toString()));return ResponseEntity.status(201).body(Map.of("id",id));}
    @PutMapping("/mantenimiento/{id}/estado") @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')") public Map<String,Object> maintenanceState(@PathVariable long id,@RequestBody Map<String,Object> b,Authentication a){String state=s(b,"estado").toUpperCase();service.maintenanceState(id,state);service.audit(a.getName(),"CAMBIAR_ESTADO","MANTENIMIENTO",(int)id,state);return Map.of("estado",state);}

    @GetMapping("/combustible") public List<Map<String,Object>> fuel(){return service.fuel();}
    @PostMapping("/combustible") public ResponseEntity<?> fuel(@RequestBody Map<String,Object> b,Authentication a){Long employee=b.containsKey("empleadoId")?l(b,"empleadoId"):null;long id=service.addFuel(l(b,"vehiculoId"),((Number)b.get("litros")).doubleValue(),((Number)b.getOrDefault("costo",0)).doubleValue(),((Number)b.getOrDefault("kilometraje",0)).intValue(),employee,String.valueOf(b.getOrDefault("observaciones","")));service.audit(a.getName(),"REGISTRAR_COMBUSTIBLE","COMBUSTIBLE",(int)id,safe(b.toString()));return ResponseEntity.status(201).body(Map.of("id",id));}

    @GetMapping("/camara/fuentes") public List<Map<String,Object>> cameraSources(){return service.cameraSources();}
    @PostMapping("/camara/fuentes") @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')") public ResponseEntity<?> cameraSource(@RequestBody Map<String,Object> b,Authentication a){long id=service.addCameraSource(s(b,"nombre"),String.valueOf(b.getOrDefault("url","")),String.valueOf(b.getOrDefault("ubicacion","")),String.valueOf(b.getOrDefault("tipo","MANUAL")));service.audit(a.getName(),"CREAR_CAMARA","CAMARA",(int)id,safe(b.toString()));return ResponseEntity.status(201).body(Map.of("id",id));}
    @PutMapping("/camara/fuentes/{id}/estado") @PreAuthorize("hasRole('ADMIN')") public Map<String,Object> cameraState(@PathVariable long id,@RequestBody Map<String,Object> b,Authentication a){boolean active=Boolean.parseBoolean(String.valueOf(b.getOrDefault("activa",true)));service.cameraSourceState(id,active);service.audit(a.getName(),"CAMBIAR_CAMARA","CAMARA",(int)id,String.valueOf(active));return Map.of("activa",active);}
    @GetMapping("/camara/detecciones") public List<Map<String,Object>> detections(@RequestParam(defaultValue="100") int limit){return service.detections(Math.min(Math.max(limit,1),500));}
    @PostMapping("/camara/detecciones/imagen") public ResponseEntity<?> detectImage(@RequestParam("imagen") MultipartFile image,Authentication a) throws Exception { if(image.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error","La imagen esta vacia")); var result=recognition.reconocer(image.getBytes()); if(result.isEmpty()) return ResponseEntity.status(422).body(Map.of("error","No hay un motor de reconocimiento de matriculas conectado","siguientePaso","Conectar una implementacion de PlateRecognitionService basada en OpenCV/OCR o un servicio externo")); PlateDetection d=result.get(); long id=service.detection(null,d.matricula(),d.confianza(),"DETECCION_IMAGEN",d.origen()); service.audit(a.getName(),"DETECTAR_MATRICULA","CAMARA",(int)id,d.matricula()); return ResponseEntity.status(201).body(Map.of("id",id,"matricula",d.matricula(),"confianza",d.confianza(),"origen",d.origen())); }
    @PostMapping("/camara/detecciones") public ResponseEntity<?> detection(@RequestBody Map<String,Object> b,Authentication a){Long source=b.containsKey("fuenteId")?l(b,"fuenteId"):null;Double conf=b.containsKey("confianza")?((Number)b.get("confianza")).doubleValue():null;String plate=s(b,"matricula").toUpperCase();String event=String.valueOf(b.getOrDefault("tipoEvento","DETECCION")).toUpperCase();long id=service.detection(source,plate,conf,event,String.valueOf(b.getOrDefault("observaciones","")));Map<String,Object> result=new LinkedHashMap<>();result.put("id",id);result.put("mensaje","Deteccion registrada");if(event.equals("SALIDA")||event.equals("ENTRADA")){result.putAll(service.processCameraEvent(plate,event));}service.audit(a.getName(),"REGISTRAR_DETECCION","CAMARA",(int)id,"matricula="+plate+" evento="+event);realtime.publish("camera",result);return ResponseEntity.status(201).body(result);}

    @GetMapping("/alertas") public List<Map<String,Object>> alerts(@RequestParam(defaultValue="true") boolean pendientes){return service.alerts(pendientes);}
    @PostMapping("/alertas") public ResponseEntity<?> alert(@RequestBody Map<String,Object> b){long id=service.alert(s(b,"tipo"),s(b,"mensaje"),String.valueOf(b.getOrDefault("severidad","INFO")));return ResponseEntity.status(201).body(Map.of("id",id));}
    @PutMapping("/alertas/{id}/resolver") public Map<String,Object> resolve(@PathVariable long id,Authentication a){service.resolveAlert(id);service.audit(a.getName(),"RESOLVER_ALERTA","ALERTA",(int)id,null);return Map.of("mensaje","Alerta resuelta");}

    @GetMapping("/viajes") public List<Map<String,Object>> trips(){return service.trips();}
    @GetMapping("/auditoria") @PreAuthorize("hasRole('ADMIN')") public List<Map<String,Object>> audit(@RequestParam(defaultValue="200") int limit){return service.audit(Math.min(Math.max(limit,1),1000));}
    @GetMapping("/usuarios") @PreAuthorize("hasRole('ADMIN')") public List<Map<String,Object>> users(){return service.users();}
    @PostMapping("/usuarios") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<?> user(@RequestBody Map<String,Object> b,Authentication a){String username=s(b,"username"),password=s(b,"password"),name=s(b,"nombre"),role=String.valueOf(b.getOrDefault("rol","EMPLEADO")).toUpperCase();if(password.length()<8)throw new IllegalArgumentException("La contrasena debe tener al menos 8 caracteres");service.createUser(username,encoder.encode(password),name,role);service.audit(a.getName(),"CREAR_USUARIO","USUARIO",null,username);return ResponseEntity.status(201).body(Map.of("mensaje","Usuario creado"));}
    @PutMapping("/usuarios/{id}/estado") @PreAuthorize("hasRole('ADMIN')") public Map<String,Object> userState(@PathVariable long id,@RequestBody Map<String,Object> b){boolean active=Boolean.parseBoolean(String.valueOf(b.getOrDefault("activo",true)));service.setUserActive(id,active);return Map.of("activo",active);}

    @GetMapping("/incidencias") public List<Map<String,Object>> incidents(){return service.incidencias();}
    @PostMapping("/incidencias") public ResponseEntity<?> incident(@RequestBody Map<String,Object> b,Authentication a){Long emp=b.containsKey("empleadoId")?l(b,"empleadoId"):null;long id=service.createIncident(l(b,"vehiculoId"),emp,s(b,"tipo"),String.valueOf(b.getOrDefault("descripcion","")),String.valueOf(b.getOrDefault("prioridad","MEDIA")).toUpperCase(),String.valueOf(b.getOrDefault("fotoRef","")));service.audit(a.getName(),"CREAR_INCIDENCIA","INCIDENCIA",(int)id,safe(b.toString()));return ResponseEntity.status(201).body(Map.of("id",id));}
    @PutMapping("/incidencias/{id}/estado") @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')") public Map<String,Object> incidentState(@PathVariable long id,@RequestBody Map<String,Object> b,Authentication a){String state=s(b,"estado").toUpperCase();service.incidentState(id,state);service.audit(a.getName(),"CAMBIAR_INCIDENCIA","INCIDENCIA",(int)id,state);return Map.of("estado",state);}

    @GetMapping("/documentos") public List<Map<String,Object>> documents(){return service.documents();}
    @PostMapping("/documentos") @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')") public ResponseEntity<?> document(@RequestBody Map<String,Object> b,Authentication a){long id=service.addDocument(l(b,"vehiculoId"),s(b,"tipo"),String.valueOf(b.getOrDefault("numero","")),String.valueOf(b.getOrDefault("fechaEmision","")),b.get("fechaVencimiento")==null?null:String.valueOf(b.get("fechaVencimiento")),String.valueOf(b.getOrDefault("archivoRef","")));service.audit(a.getName(),"CREAR_DOCUMENTO","DOCUMENTO",(int)id,safe(b.toString()));return ResponseEntity.status(201).body(Map.of("id",id));}
    @PutMapping("/documentos/{id}/estado") @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')") public Map<String,Object> documentState(@PathVariable long id,@RequestBody Map<String,Object> b){String state=s(b,"estado").toUpperCase();service.documentState(id,state);return Map.of("estado",state);}

    @GetMapping("/checklists") public List<Map<String,Object>> checklists(){return service.checklists();}
    @PostMapping("/checklists") public ResponseEntity<?> checklist(@RequestBody Map<String,Object> b,Authentication a){Long emp=b.containsKey("empleadoId")?l(b,"empleadoId"):null;Long trip=b.containsKey("viajeId")?l(b,"viajeId"):null;String items=s(b,"itemsJson");long id=service.addChecklist(l(b,"vehiculoId"),emp,trip,String.valueOf(b.getOrDefault("resultado","APROBADO")),items,String.valueOf(b.getOrDefault("observaciones","")));service.audit(a.getName(),"CREAR_CHECKLIST","CHECKLIST",(int)id,null);return ResponseEntity.status(201).body(Map.of("id",id));}

    @GetMapping("/control-acceso") public List<Map<String,Object>> access(@RequestParam(defaultValue="100") int limit){return service.accessEvents(Math.min(Math.max(limit,1),500));}
    @PostMapping("/control-acceso") public ResponseEntity<?> access(@RequestBody Map<String,Object> b,Authentication a){Long veh=b.containsKey("vehiculoId")?l(b,"vehiculoId"):null;Long emp=b.containsKey("empleadoId")?l(b,"empleadoId"):null;Long cam=b.containsKey("camaraId")?l(b,"camaraId"):null;long id=service.accessEvent(veh,emp,String.valueOf(b.getOrDefault("matricula","")),s(b,"metodo"),s(b,"direccion"),s(b,"resultado"),String.valueOf(b.getOrDefault("motivo","")),cam);service.audit(a.getName(),"CONTROL_ACCESO","ACCESO",(int)id,safe(b.toString()));realtime.publish("access",b);return ResponseEntity.status(201).body(Map.of("id",id));}

    @GetMapping("/gps/{vehiculoId}") public List<Map<String,Object>> gps(@PathVariable long vehiculoId,@RequestParam(defaultValue="100") int limit){return service.gps(vehiculoId,Math.min(Math.max(limit,1),1000));}
    @PostMapping("/gps") public ResponseEntity<?> gps(@RequestBody Map<String,Object> b,Authentication a){long id=service.gpsPosition(l(b,"vehiculoId"),((Number)b.get("latitud")).doubleValue(),((Number)b.get("longitud")).doubleValue(),((Number)b.getOrDefault("velocidad",0)).doubleValue(),((Number)b.getOrDefault("rumbo",0)).doubleValue(),String.valueOf(b.getOrDefault("fuente","GPS")));service.audit(a.getName(),"REGISTRAR_GPS","VEHICULO",((Number)b.get("vehiculoId")).intValue(),null);return ResponseEntity.status(201).body(Map.of("id",id));}

    @GetMapping("/analitica") public List<Map<String,Object>> analytics(){return service.analytics();}
    @PostMapping("/camara/procesar") public Map<String,Object> processCamera(@RequestBody Map<String,Object> b,Authentication a){String plate=s(b,"matricula").toUpperCase();String direction=s(b,"direccion").toUpperCase();Map<String,Object> result=service.processCameraEvent(plate,direction);service.audit(a.getName(),"PROCESAR_EVENTO_CAMARA","CAMARA",null,plate+" "+direction+" "+result);return result;}

    @GetMapping("/empresas") @PreAuthorize("hasRole('ADMIN')") public List<Map<String,Object>> companies(){return service.companies();}
    @PostMapping("/empresas") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<?> company(@RequestBody Map<String,Object> b){long id=service.createCompany(s(b,"nombre"),s(b,"identificador"));return ResponseEntity.status(201).body(Map.of("id",id));}
    @GetMapping("/sedes") @PreAuthorize("hasRole('ADMIN')") public List<Map<String,Object>> sites(){return service.sites();}
    @PostMapping("/sedes") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<?> site(@RequestBody Map<String,Object> b){long id=service.createSite(l(b,"empresaId"),s(b,"nombre"),String.valueOf(b.getOrDefault("direccion","")));return ResponseEntity.status(201).body(Map.of("id",id));}
    @GetMapping("/webhooks") @PreAuthorize("hasRole('ADMIN')") public List<Map<String,Object>> webhooks(){return service.webhooks();}
    @PostMapping("/webhooks") @PreAuthorize("hasRole('ADMIN')") public ResponseEntity<?> webhook(@RequestBody Map<String,Object> b){long id=service.createWebhook(s(b,"nombre"),s(b,"url"),String.valueOf(b.getOrDefault("secreto","")),s(b,"eventos"));return ResponseEntity.status(201).body(Map.of("id",id));}

    @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<?> bad(IllegalArgumentException e){return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));}
    private static String safe(String x){return x.length()>1000?x.substring(0,1000):x;}
}
