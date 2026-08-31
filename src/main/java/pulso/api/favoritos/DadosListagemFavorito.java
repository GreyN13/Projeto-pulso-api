package pulso.api.favoritos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;

@Schema(name = "FavoritoResposta", description = "Uma música favoritada pelo usuário autenticado.")
public record DadosListagemFavorito(Long id, Long musicaId, Date criadoEm) {
    public DadosListagemFavorito(Favorito dados) {
        this(dados.getId(), dados.getMusica().getId(), dados.getCriadoEm());
    }
}
