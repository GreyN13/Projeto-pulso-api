package pulso.api.musicas;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DadosCadastroMusica(
        @NotNull(message = "Álbum é obrigatório")
        @Positive(message = "Identificador do álbum deve ser maior que zero") Long albumId,
        @NotBlank(message = "Título da música é obrigatório") String titulo,
        @NotBlank(message = "URL do arquivo da música é obrigatória") String arquivoUrl,
        @Positive(message = "Duração em segundos deve ser maior que zero") int duracaoSegundos
) {
}
