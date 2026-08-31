package pulso.api.albuns;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DadosCadastroAlbum(
        @NotBlank(message = "Título do álbum é obrigatório")
        @Size(max = 150, message = "Título do álbum deve ter no máximo 150 caracteres") String titulo,
        @Size(max = 500, message = "URL da capa deve ter no máximo 500 caracteres") String capaUrl,
        @Positive(message = "Ano deve ser maior que zero") int ano,
        @NotNull(message = "Artista é obrigatório")
        @Positive(message = "Identificador do artista deve ser maior que zero") Long artistaId
) {
}
