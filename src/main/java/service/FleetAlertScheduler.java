package service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import database.EnterpriseDAO;

@Component
public class FleetAlertScheduler {
    private final EnterpriseService service;
    public FleetAlertScheduler(EnterpriseService service){this.service=service;}
    @Scheduled(fixedDelayString="${fleet.alerts.interval-ms:900000}")
    public void checkFleet(){
        for(var v:EnterpriseDAO.query("SELECT id,matricula,combustible_porcentaje FROM vehiculos WHERE activo=1 AND combustible_porcentaje<=15")){
            String plate=String.valueOf(v.get("matricula"));
            long exists=((Number)EnterpriseDAO.query("SELECT COUNT(*) c FROM alertas WHERE tipo='COMBUSTIBLE_BAJO' AND mensaje LIKE ? AND resuelta=0", "%"+plate+"%").get(0).get("c")).longValue();
            if(exists==0) service.alert("COMBUSTIBLE_BAJO","Combustible bajo en "+plate,"AVISO");
        }
        for(var m:EnterpriseDAO.query("SELECT m.id,v.matricula,m.fecha FROM mantenimientos m JOIN vehiculos v ON v.id=m.vehiculo_id WHERE m.estado='PROGRAMADO' AND date(m.fecha)<=date('now','localtime','+7 day')")){
            String plate=String.valueOf(m.get("matricula"));
            long exists=((Number)EnterpriseDAO.query("SELECT COUNT(*) c FROM alertas WHERE tipo='MANTENIMIENTO_PROXIMO' AND mensaje LIKE ? AND resuelta=0", "%"+plate+"%").get(0).get("c")).longValue();
            if(exists==0) service.alert("MANTENIMIENTO_PROXIMO","Mantenimiento próximo de "+plate,"INFO");
        }
    }
}
