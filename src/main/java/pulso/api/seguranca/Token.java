package pulso.api.seguranca;

import java.util.Date;

public record Token(String token, String tipo, Date expiraEm) {
}
