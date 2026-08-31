package pulso.api.artistas;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ArtistaResposta", description = "Dados públicos de um artista do catálogo.")
public record DadosListagemArtista(Long id, String nome, String biografia, String imagemUrl) {
    public DadosListagemArtista(Artista dados) {
        this(dados.getId(), dados.getNome(), dados.getBiografia(), dados.getImagemUrl());
    }
}
