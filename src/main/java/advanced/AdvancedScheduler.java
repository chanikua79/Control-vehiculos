package advanced;

import database.EnterpriseDAO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AdvancedScheduler {
    private final AdvancedService service;
    public AdvancedScheduler(AdvancedService service){this.service=service;}

    @Scheduled(fixedDelayString="${fleet.advanced.alerts.interval-ms:900000}")
    public void checkAdvancedAlerts(){
        for(var d:EnterpriseDAO.query("SELECT d.id,v.matricula,d.tipo,d.fecha_vencimiento FROM documentos_vehiculo d JOIN vehiculos v ON v.id=d.vehiculo_id WHERE d.fecha_vencimiento IS NOT NULL AND date(d.fecha_vencimiento)<=date('now','localtime','+30 day') AND d.estado<>'VENCIDO'")){
            String plate=String.valueOf(d.get("matricula"));
            if(!existsAlert("DOCUMENTO_PROXIMO",plate)) service.alert("DOCUMENTO_PROXIMO","Documento "+d.get("tipo")+" de "+plate+" vence el "+d.get("fecha_vencimiento"),"AVISO");
        }
        for(var p:AdvancedDAO.query("SELECT p.empleado_id,e.nombre,p.licencia_vencimiento FROM perfiles_conductor p JOIN empleados e ON e.id=p.empleado_id WHERE p.licencia_vencimiento IS NOT NULL AND date(p.licencia_vencimiento)<=date('now','localtime','+30 day')")){
            String name=String.valueOf(p.get("nombre"));
            if(!existsAlert("LICENCIA_PROXIMA",name)) service.alert("LICENCIA_PROXIMA","Licencia de "+name+" vence el "+p.get("licencia_vencimiento"),"AVISO");
        }
        for(var k:AdvancedDAO.query("SELECT k.codigo,v.matricula FROM llaves_vehiculo k JOIN vehiculos v ON v.id=k.vehiculo_id WHERE k.estado='ENTREGADA' AND k.entregada_en IS NOT NULL AND datetime(k.entregada_en)<datetime('now','localtime','-24 hour')")){
            String code=String.valueOf(k.get("codigo"));
            if(!existsAlert("LLAVE_NO_DEVUELTA",code)) service.alert("LLAVE_NO_DEVUELTA","La llave "+code+" de "+k.get("matricula")+" lleva más de 24 horas entregada","AVISO");
        }
    }
    private boolean existsAlert(String type,String text){return ((Number)EnterpriseDAO.query("SELECT COUNT(*) c FROM alertas WHERE tipo=? AND mensaje LIKE ? AND resuelta=0",type,"%"+text+"%").get(0).get("c")).longValue()>0;}
}
