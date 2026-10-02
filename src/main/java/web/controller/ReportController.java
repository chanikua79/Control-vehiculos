package web.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import service.EnterpriseService;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/reportes")
public class ReportController {
    private final EnterpriseService service;
    public ReportController(EnterpriseService service){this.service=service;}
    @GetMapping(value="/viajes.csv",produces="text/csv") public ResponseEntity<ByteArrayResource> viajes(){
        StringBuilder s=new StringBuilder("id,matricula,empleado,salida,entrada,duracion_minutos\n");
        for(var r:service.trips()) s.append(csv(r.get("id"))).append(',').append(csv(r.get("matricula"))).append(',').append(csv(r.get("empleado_nombre"))).append(',').append(csv(r.get("salida"))).append(',').append(csv(r.get("entrada"))).append(',').append(csv(r.get("duracion_minutos"))).append('\n');
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=viajes.csv").contentType(MediaType.parseMediaType("text/csv")).body(new ByteArrayResource(s.toString().getBytes(StandardCharsets.UTF_8)));
    }
    private static String csv(Object x){String v=x==null?"":String.valueOf(x);return "\""+v.replace("\"","\"\"")+"\"";}
}
