package pulso.api.infra;

import java.util.List;
import java.time.LocalDateTime;

public record ErroResposta(
        int status,
        String erro,
        List<ErroCampo> campos,
        LocalDateTime instante
) {
}
