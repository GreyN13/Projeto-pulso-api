package pulso.api.seguranca;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class TokenService {
    private static final long TEMPO_EXPIRACAO = 86_400_000L;
    private static final Key CHAVE_SECRETA = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    

    public String gerarToken(String email) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + TEMPO_EXPIRACAO);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(agora)
                .setExpiration(expiracao)
                .signWith(CHAVE_SECRETA)
                .compact();
    }

    public boolean isTokenValido(String token) {
        try {
            getUsuario(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public String getUsuario(String token) {
        return Jwts.parser()
                .setSigningKey(CHAVE_SECRETA)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Date getDataExpiracao(String token) {
        return Jwts.parser()
                .setSigningKey(CHAVE_SECRETA)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }
}
