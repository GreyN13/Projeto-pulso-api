package pulso.api.artistas;

import jakarta.validation.constraints.NotBlank;

public record DadosCadastroArtista(@NotBlank(message = "Nome do artista é obrigatório") String nome, String biografia, String imagemUrl) {
}
