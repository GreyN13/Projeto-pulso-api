package pulso.api.seguranca;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pulso.api.usuarios.UsuarioRepositorio;

import java.util.Collections;

@Service
public class AutenticacaoService implements UserDetailsService {
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepositorio.findByEmailIgnoreCase(username)
                .map(usuario -> new User(usuario.getEmail(), usuario.getSenha(), Collections.emptyList()))
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}
