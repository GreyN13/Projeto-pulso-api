package pulso.api.musicas;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DadosAlteracaoMusica(
        @Positive(message = "Identificador do álbum deve ser maior que zero") Long albumId,
        @Pattern(regexp = ".*\\S.*", message = "Título da música não pode ficar em branco")
        @Size(max = 150, message = "Título da música deve ter no máximo 150 caracteres") String titulo,
        @Pattern(regexp = ".*\\S.*", message = "URL do arquivo da música não pode ficar em branco")
        @Size(max = 500, message = "URL do arquivo da música deve ter no máximo 500 caracteres") String arquivoUrl,
        @Positive(message = "Duração em segundos deve ser maior que zero") Integer duracaoSegundos
) {
}
