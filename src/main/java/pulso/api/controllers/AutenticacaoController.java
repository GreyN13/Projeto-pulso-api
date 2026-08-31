package pulso.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pulso.api.infra.LimiteTentativasExcedidoException;
import pulso.api.seguranca.Login;
import pulso.api.seguranca.LimitadorTentativasLogin;
import pulso.api.seguranca.Token;
import pulso.api.seguranca.TokenService;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Login e emissão de token JWT.")
public class AutenticacaoController {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final LimitadorTentativasLogin limitadorTentativasLogin;

    public AutenticacaoController(AuthenticationManager authenticationManager, TokenService tokenService,
                                  LimitadorTentativasLogin limitadorTentativasLogin) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.limitadorTentativasLogin = limitadorTentativasLogin;
    }

    @PostMapping
    @Operation(
            summary = "Autentica usuário",
            description = "Valida e-mail e senha e devolve um JWT para as rotas privadas."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Token JWT emitido",
            content = @Content(examples = @ExampleObject(
                    value = "{\"token\":\"eyJ...\",\"tipo\":\"Bearer\",\"expiraEm\":\"2026-08-06T10:00:00\"}"
            ))
    )
    @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos")
    @ApiResponse(responseCode = "429", description = "Limite de tentativas de login excedido")
    public ResponseEntity<Token> autenticar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(
                            value = "{\"email\":\"ana@pulso.com\",\"senha\":\"Ana@1234\"}"
                    ))
            )
            @RequestBody @Valid Login dados,
            HttpServletRequest request
    ) {
        if (!limitadorTentativasLogin.tentativaPermitida(request.getRemoteAddr())) {
            throw new LimiteTentativasExcedidoException("Limite de tentativas excedido. Tente novamente em uma hora");
        }

        Authentication autenticacao = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dados.email(), dados.senha())
        );
        String jwt = tokenService.gerarToken(autenticacao.getName());
        return ResponseEntity.ok(new Token(jwt, "Bearer", tokenService.getDataExpiracao(jwt)));
    }
}
