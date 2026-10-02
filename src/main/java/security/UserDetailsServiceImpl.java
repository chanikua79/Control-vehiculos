package security;

import database.EnterpriseDAO;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Override public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var rows=EnterpriseDAO.query("SELECT username,password_hash,nombre,rol,activo FROM usuarios WHERE username=?",username);
        if(rows.isEmpty()) throw new UsernameNotFoundException("Usuario no encontrado");
        var r=rows.get(0);
        boolean active=((Number)r.get("activo")).intValue()==1;
        return User.withUsername((String)r.get("username"))
                .password((String)r.get("password_hash"))
                .roles(String.valueOf(r.get("rol")))
                .disabled(!active).build();
    }
}
