package pulso.api.musicas;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DadosCadastroMusica(
        @NotNull(message = "Álbum é obrigatório")
        @Positive(message = "Identificador do álbum deve ser maior que zero") Long albumId,
        @NotBlank(message = "Título da música é obrigatório")
        @Size(max = 150, message = "Título da música deve ter no máximo 150 caracteres") String titulo,
        @NotBlank(message = "URL do arquivo da música é obrigatória")
        @Size(max = 500, message = "URL do arquivo da música deve ter no máximo 500 caracteres") String arquivoUrl,
        @Positive(message = "Duração em segundos deve ser maior que zero") int duracaoSegundos
) {
}
