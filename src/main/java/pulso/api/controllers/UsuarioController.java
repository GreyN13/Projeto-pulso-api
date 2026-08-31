package pulso.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pulso.api.usuarios.*;
import pulso.api.infra.ConflitoException;

import java.net.URI;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "Cadastro de usuários da plataforma.")
public class UsuarioController {
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping
    @Transactional
    @Operation(summary = "Cadastra usuário", description = "Cria uma conta com e-mail único e senha armazenada com BCrypt.")
    @ApiResponse(responseCode = "201", description = "Usuário cadastrado", content = @Content(schema = @Schema(implementation = DadosListagemUsuario.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
    public ResponseEntity<DadosListagemUsuario> cadastrar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(
                            value = "{\"nome\":\"Ana\",\"email\":\"ana@pulso.com\",\"senha\":\"Ana@1234\"}"
                    ))
            )
            @RequestBody @Valid DadosCadastroUsuario dados
    ) {
        if (usuarioRepositorio.existsByEmailIgnoreCase(dados.email())) {
            throw new ConflitoException("Já existe um usuário cadastrado com este e-mail");
        }

        Usuario u = usuarioRepositorio.save(new Usuario(dados, passwordEncoder.encode(dados.senha())));
        Long id = u.getId();
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(uri).body(new DadosListagemUsuario(u));
    }
}
