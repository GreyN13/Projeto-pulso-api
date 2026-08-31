package pulso.api.artistas;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosCadastroArtista(
        @NotBlank(message = "Nome do artista é obrigatório")
        @Size(max = 150, message = "Nome do artista deve ter no máximo 150 caracteres") String nome,
        String biografia,
        @Size(max = 500, message = "URL da imagem deve ter no máximo 500 caracteres") String imagemUrl
) {
}
