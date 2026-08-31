package pulso.api.playlists;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosCadastroPlaylist(
        @NotBlank(message = "Nome da playlist é obrigatório")
        @Size(min = 2, max = 80, message = "Nome da playlist deve ter entre 2 e 80 caracteres") String nome,
        @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres") String descricao
) {
}
