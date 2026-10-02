package web.controller;

import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.*;
import java.util.*;
import database.EnterpriseDAO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager manager;
    private final PasswordEncoder encoder;
    private final HttpSessionSecurityContextRepository repository=new HttpSessionSecurityContextRepository();
    public AuthController(AuthenticationManager manager,PasswordEncoder encoder){this.manager=manager;this.encoder=encoder;}
    @GetMapping("/csrf") public Map<String,String> csrf(CsrfToken token){ return Map.of("token",token.getToken()); }
    @PostMapping("/login") public ResponseEntity<?> login(@RequestBody Map<String,String> body,HttpServletRequest req,HttpServletResponse res){
        try{
            var token=new UsernamePasswordAuthenticationToken(body.get("username"),body.get("password"));
            var auth=manager.authenticate(token); var ctx=SecurityContextHolder.createEmptyContext(); ctx.setAuthentication(auth); SecurityContextHolder.setContext(ctx); repository.saveContext(ctx,req,res);
            return ResponseEntity.ok(Map.of("autenticado",true,"usuario",auth.getName(),"roles",auth.getAuthorities().stream().map(a->a.getAuthority()).toList()));
        }catch(AuthenticationException e){return ResponseEntity.status(401).body(Map.of("error","Credenciales invalidas"));}
    }
    @GetMapping("/me") public ResponseEntity<?> me(Authentication auth){if(auth==null)return ResponseEntity.status(401).body(Map.of("autenticado",false));return ResponseEntity.ok(Map.of("autenticado",true,"usuario",auth.getName(),"roles",auth.getAuthorities().stream().map(a->a.getAuthority()).toList()));}
    @PutMapping("/password") public ResponseEntity<?> password(Authentication auth,@RequestBody Map<String,String> body){String old=body.get("actual"), next=body.get("nueva"); var rows=EnterpriseDAO.query("SELECT password_hash FROM usuarios WHERE username=?",auth.getName()); if(rows.isEmpty()||!encoder.matches(old,String.valueOf(rows.get(0).get("password_hash")))) return ResponseEntity.status(400).body(Map.of("error","Contrasena actual incorrecta")); if(next==null||next.length()<8)return ResponseEntity.badRequest().body(Map.of("error","La nueva contrasena debe tener al menos 8 caracteres")); EnterpriseDAO.execute("UPDATE usuarios SET password_hash=? WHERE username=?",encoder.encode(next),auth.getName()); return ResponseEntity.ok(Map.of("mensaje","Contrasena actualizada"));}
    @PostMapping("/logout") public ResponseEntity<?> logout(HttpServletRequest req){req.getSession(false); SecurityContextHolder.clearContext(); var s=req.getSession(false); if(s!=null)s.invalidate(); return ResponseEntity.ok(Map.of("mensaje","Sesion cerrada"));}
}
