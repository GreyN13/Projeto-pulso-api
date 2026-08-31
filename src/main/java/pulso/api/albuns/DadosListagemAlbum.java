package pulso.api.albuns;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AlbumResposta", description = "Dados públicos de um álbum do catálogo.")
public record DadosListagemAlbum(Long id, String titulo, String capaUrl, int ano, Long artistaId) {
    public DadosListagemAlbum(Album dados) {
        this(dados.getId(), dados.getTitulo(), dados.getCapaUrl(), dados.getAno(), dados.getArtista().getId());
    }
}
