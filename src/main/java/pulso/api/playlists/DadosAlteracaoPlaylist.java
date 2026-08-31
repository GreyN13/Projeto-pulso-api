package pulso.api.playlists;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DadosAlteracaoPlaylist(
        @Pattern(regexp = ".*\\S.*", message = "Nome da playlist não pode ficar em branco")
        @Size(min = 2, max = 80, message = "Nome da playlist deve ter entre 2 e 80 caracteres") String nome,
        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres") String descricao
) {
}
