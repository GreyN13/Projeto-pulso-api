package pulso.api.usuarios;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosCadastroUsuario(
        @NotBlank(message = "Nome é obrigatório") String nome,
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail deve ter um formato válido") String email,
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres") String senha
) {
}
