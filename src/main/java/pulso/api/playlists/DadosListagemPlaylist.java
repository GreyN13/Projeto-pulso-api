package pulso.api.playlists;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;

@Schema(name = "PlaylistResposta", description = "Dados de uma playlist do usuário autenticado.")
public record DadosListagemPlaylist(Long id, String nome, String descricao, Date criadaEm) {
    public DadosListagemPlaylist(Playlist dados) {
        this(dados.getId(), dados.getNome(), dados.getDescricao(), dados.getCriadaEm());
    }
}
