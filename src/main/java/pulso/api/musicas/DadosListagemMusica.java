package pulso.api.musicas;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MusicaResposta", description = "Dados públicos de uma música do catálogo.")
public record DadosListagemMusica(Long id, Long albumId, String titulo, String arquivoUrl, int duracaoSegundos) {
    public DadosListagemMusica(Musica dados) {
        this(dados.getId(), dados.getAlbum().getId(), dados.getTitulo(), dados.getArquivoUrl(), dados.getDuracaoSegundos());
    }
}
