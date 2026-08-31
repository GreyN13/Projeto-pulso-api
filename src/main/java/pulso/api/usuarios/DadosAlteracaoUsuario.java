package pulso.api.usuarios;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DadosAlteracaoUsuario(
        @Pattern(regexp = ".*\\S.*", message = "Nome não pode ficar em branco")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres") String nome,
        @Pattern(regexp = ".*\\S.*", message = "E-mail não pode ficar em branco")
        @Email(message = "E-mail deve ter um formato válido") String email
) {
}
