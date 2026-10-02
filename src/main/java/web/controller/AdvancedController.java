package web.controller;

import advanced.AdvancedService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import service.RealtimeEventService;
import java.util.*;

@RestController
@RequestMapping("/api/advanced")
public class AdvancedController {
    private final AdvancedService service; private final RealtimeEventService realtime;
    public AdvancedController(AdvancedService service,RealtimeEventService realtime){this.service=service;this.realtime=realtime;}
    @GetMapping("/keys") public List<Map<String,Object>> keys(){return service.keys();}
    @PostMapping("/keys") @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')") public Map<String,Object> key(@RequestBody Map<String,Object>b){return Map.of("id",service.createKey(n(b,"vehiculoId"),s(b,"codigo")));}
    @PostMapping("/keys/{id}/deliver") public Map<String,Object> deliver(@PathVariable long id,@RequestBody Map<String,Object>b){service.deliverKey(id,n(b,"empleadoId"),s0(b,"observaciones"));return Map.of("ok",true);}
    @PostMapping("/keys/{id}/return") public Map<String,Object> ret(@PathVariable long id,@RequestBody Map<String,Object>b){service.returnKey(id,s0(b,"observaciones"));return Map.of("ok",true);}

    @GetMapping("/inspections") public List<Map<String,Object>> inspections(){return service.inspections();}
    @PostMapping("/inspections") public ResponseEntity<?> inspection(@RequestBody Map<String,Object>b){long id=service.inspection(n(b,"vehiculoId"),ln(b,"empleadoId"),ln(b,"viajeId"),s(b,"tipo"),s0(b,"observaciones"),s0(b,"fotosJson"),s0(b,"resultado"));return ResponseEntity.status(201).body(Map.of("id",id));}
    @GetMapping("/geofences/check/{vehiculoId}") public List<Map<String,Object>> checkGeofences(@PathVariable long vehiculoId,@RequestParam double lat,@RequestParam double lon){return service.checkGeofences(vehiculoId,lat,lon);}
    @GetMapping("/geofences") public List<Map<String,Object>> geofences(){return service.geofences();}
    @PostMapping("/geofences") @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')") public Map<String,Object> geofence(@RequestBody Map<String,Object>b){return Map.of("id",service.geofence(s(b,"nombre"),d(b,"latitud"),d(b,"longitud"),d(b,"radioMetros")));}
    @PutMapping("/geofences/{id}/state") @PreAuthorize("hasRole('ADMIN')") public Map<String,Object> geofenceState(@PathVariable long id,@RequestBody Map<String,Object>b){service.geofenceState(id,bool(b,"activa"));return Map.of("ok",true);}

    @GetMapping("/rules") public List<Map<String,Object>> rules(){return service.rules();}
    @PostMapping("/rules") @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')") public Map<String,Object> rule(@RequestBody Map<String,Object>b){return Map.of("id",service.rule(s(b,"nombre"),s(b,"evento"),s(b,"condicion"),s(b,"accion"),s0(b,"severidad")));}
    @PutMapping("/rules/{id}/state") @PreAuthorize("hasRole('ADMIN')") public Map<String,Object> ruleState(@PathVariable long id,@RequestBody Map<String,Object>b){service.ruleState(id,bool(b,"activa"));return Map.of("ok",true);}
    @GetMapping("/schedules") public List<Map<String,Object>> schedules(){return service.schedules();}
    @PostMapping("/schedules") @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')") public Map<String,Object> schedule(@RequestBody Map<String,Object>b){return Map.of("id",service.schedule(s(b,"nombre"),n(b,"diaSemana").intValue(),s(b,"inicio"),s(b,"fin"),bool(b,"requiereAutorizacion")));}
    @GetMapping("/holidays") public List<Map<String,Object>> holidays(){return service.holidays();}
    @PostMapping("/holidays") @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')") public Map<String,Object> holiday(@RequestBody Map<String,Object>b){return Map.of("id",service.holiday(s(b,"fecha"),s(b,"nombre"),bool(b,"requiereAutorizacion")));}

    @GetMapping("/approvals") public List<Map<String,Object>> approvals(){return service.approvals();}
    @PostMapping("/approvals") public Map<String,Object> approval(@RequestBody Map<String,Object>b,Authentication a){return Map.of("id",service.approval(s(b,"tipo"),n(b,"referenciaId"),a.getName()));}
    @PutMapping("/approvals/{id}") @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')") public Map<String,Object> approvalState(@PathVariable long id,@RequestBody Map<String,Object>b,Authentication a){service.approvalState(id,s(b,"estado"),a.getName(),s0(b,"comentario"));realtime.publish("approval",b);return Map.of("ok",true);}
    @GetMapping("/notifications") public List<Map<String,Object>> notifications(Authentication a){return service.notifications(a.getName());}
    @PostMapping("/notifications/{id}/read") public Map<String,Object> read(@PathVariable long id){service.markRead(id);return Map.of("ok",true);}

    @GetMapping("/photos") public List<Map<String,Object>> photos(){return service.photos();}
    @PostMapping("/photos") public Map<String,Object> photo(@RequestBody Map<String,Object>b){return Map.of("id",service.photo(s(b,"entidad"),n(b,"entidadId"),s(b,"tipo"),s(b,"referencia")));}
    @GetMapping("/devices") public List<Map<String,Object>> devices(){return service.devices();}
    @PostMapping("/devices") @PreAuthorize("hasRole('ADMIN')") public Map<String,Object> device(@RequestBody Map<String,Object>b){return Map.of("id",service.device(s(b,"nombre"),s(b,"tipo"),s(b,"identificador"),s0(b,"ubicacion")));}
    @PostMapping("/devices/{id}/heartbeat") public Map<String,Object> heartbeat(@PathVariable long id){service.deviceHeartbeat(id);return Map.of("ok",true);}

    @GetMapping("/modules") public List<Map<String,Object>> modules(){return service.modules();}
    @PutMapping("/modules/{name}") @PreAuthorize("hasRole('ADMIN')") public Map<String,Object> module(@PathVariable String name,@RequestBody Map<String,Object>b){service.module(name,bool(b,"habilitado"),s0(b,"configuracionJson"));return Map.of("ok",true);}
    @GetMapping("/subscriptions") @PreAuthorize("hasRole('ADMIN')") public List<Map<String,Object>> subscriptions(){return service.subscriptions();}
    @PostMapping("/subscriptions") @PreAuthorize("hasRole('ADMIN')") public Map<String,Object> subscription(@RequestBody Map<String,Object>b){return Map.of("id",service.subscription(n(b,"empresaId"),s(b,"plan"),s0(b,"estado"),s(b,"inicio"),s0(b,"fin"),n(b,"limiteVehiculos").intValue(),n(b,"limiteUsuarios").intValue()));}
    @GetMapping("/conductores") public List<Map<String,Object>> driverProfiles(){return service.driverProfiles();}
    @PutMapping("/conductores/{empleadoId}") @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')") public Map<String,Object> driverProfile(@PathVariable long empleadoId,@RequestBody Map<String,Object>b){service.driverProfile(empleadoId,s0(b,"licenciaNumero"),s0(b,"categoria"),s0(b,"licenciaVencimiento"),s0(b,"restricciones"),s0(b,"telefono"),s0(b,"correo"));return Map.of("ok",true);}
    @GetMapping("/rutas/{vehiculoId}") public List<Map<String,Object>> routes(@PathVariable long vehiculoId){return service.routes(vehiculoId);}
    @PostMapping("/rutas") public Map<String,Object> route(@RequestBody Map<String,Object>b){return Map.of("id",service.route(n(b,"vehiculoId"),s(b,"nombre"),s0(b,"inicio"),s0(b,"fin"),d(b,"distanciaKm"),n(b,"duracionMinutos").intValue(),s0(b,"puntosJson")));}
    @GetMapping("/api-keys") @PreAuthorize("hasRole('ADMIN')") public List<Map<String,Object>> apiKeys(){return service.apiKeys();}
    @PostMapping("/api-keys") @PreAuthorize("hasRole('ADMIN')") public Map<String,Object> apiKey(@RequestBody Map<String,Object>b){return service.createApiKey(s(b,"nombre"),s0(b,"permisos"),s0(b,"expiraEn"));}
    @PutMapping("/api-keys/{id}/state") @PreAuthorize("hasRole('ADMIN')") public Map<String,Object> apiKeyState(@PathVariable long id,@RequestBody Map<String,Object>b){service.apiKeyState(id,bool(b,"activa"));return Map.of("ok",true);}
    @GetMapping("/telemetry/{vehiculoId}") public List<Map<String,Object>> telemetry(@PathVariable long vehiculoId){return service.telemetry(vehiculoId);}
    @PostMapping("/telemetry") public Map<String,Object> telemetry(@RequestBody Map<String,Object>b){return Map.of("id",service.telemetry(b.containsKey("vehiculoId")?n(b,"vehiculoId"):null,s(b,"tipo"),d(b,"valor"),s0(b,"unidad"),s0(b,"origen")));}

    @PostMapping("/qr/temporary") public Map<String,Object> temporaryQr(@RequestBody Map<String,Object>b){return service.temporaryQr(s(b,"sujeto"),n(b,"segundos").intValue());}
    @PostMapping("/qr/consume") public Map<String,Object> consumeQr(@RequestBody Map<String,Object>b){return service.consumeQr(s(b,"token"));}
    @PostMapping("/assistant") public Map<String,Object> assistant(@RequestBody Map<String,Object>b){return service.assistant(s0(b,"pregunta"));}

    private static String s(Map<String,Object>b,String k){String x=String.valueOf(b.getOrDefault(k,""));if(x.isBlank())throw new IllegalArgumentException("El campo "+k+" es obligatorio");return x.trim();}
    private static String s0(Map<String,Object>b,String k){return String.valueOf(b.getOrDefault(k,""));}
    private static Long n(Map<String,Object>b,String k){Object x=b.get(k);if(!(x instanceof Number))throw new IllegalArgumentException("El campo "+k+" debe ser numerico");return ((Number)x).longValue();}
    private static Long ln(Map<String,Object>b,String k){return b.containsKey(k)&&b.get(k)!=null?n(b,k).longValue():null;}
    private static double d(Map<String,Object>b,String k){return n(b,k).doubleValue();}
    private static boolean bool(Map<String,Object>b,String k){Object x=b.get(k);return x instanceof Boolean?(Boolean)x:Boolean.parseBoolean(String.valueOf(x));}
}
