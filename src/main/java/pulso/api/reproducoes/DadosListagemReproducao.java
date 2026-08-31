package pulso.api.reproducoes;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;

@Schema(name = "ReproducaoResposta", description = "Registro de reprodução do usuário autenticado.")
public record DadosListagemReproducao(Long id, Long musicaId, Date reproduzidaEm) {
    public DadosListagemReproducao(Reproducao dados) {
        this(dados.getId(), dados.getMusica().getId(), dados.getReproduzidaEm());
    }
}
