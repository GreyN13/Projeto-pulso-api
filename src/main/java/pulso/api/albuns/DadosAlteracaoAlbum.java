package pulso.api.albuns;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DadosAlteracaoAlbum(
        @Pattern(regexp = ".*\\S.*", message = "Título do álbum não pode ficar em branco")
        @Size(max = 150, message = "Título do álbum deve ter no máximo 150 caracteres") String titulo,
        @Size(max = 500, message = "URL da capa deve ter no máximo 500 caracteres") String capaUrl,
        @Positive(message = "Ano deve ser maior que zero") Integer ano,
        @Positive(message = "Identificador do artista deve ser maior que zero") Long artistaId
) {
}
