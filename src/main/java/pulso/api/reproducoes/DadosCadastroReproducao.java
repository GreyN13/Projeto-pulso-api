package pulso.api.reproducoes;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DadosCadastroReproducao(
        @NotNull(message = "Música é obrigatória")
        @Positive(message = "Identificador da música deve ser maior que zero") Long musicaId
) {
}
