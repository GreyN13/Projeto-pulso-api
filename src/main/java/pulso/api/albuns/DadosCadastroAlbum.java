package pulso.api.albuns;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DadosCadastroAlbum(
        @NotBlank(message = "Título do álbum é obrigatório") String titulo,
        String capaUrl,
        @Positive(message = "Ano deve ser maior que zero") int ano,
        @NotNull(message = "Artista é obrigatório")
        @Positive(message = "Identificador do artista deve ser maior que zero") Long artistaId
) {
}
