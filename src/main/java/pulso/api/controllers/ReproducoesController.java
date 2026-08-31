package pulso.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pulso.api.infra.RegistroNaoEncontradoException;
import pulso.api.reproducoes.*;
import pulso.api.musicas.MusicaRepositorio;
import pulso.api.usuarios.Usuario;
import pulso.api.usuarios.UsuarioRepositorio;

import java.net.URI;

@RestController
@RequestMapping("/reproducoes")
@Tag(name = "Reproduções", description = "Registro de plays e histórico particular do usuário autenticado.")
public class ReproducoesController {
    @Autowired
    private ReproducaoRepositorio reproducaoRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private MusicaRepositorio musicaRepositorio;

    @PostMapping
    @Transactional
    @Operation(summary = "Registra reprodução", description = "Registra usuário, música e data/hora atual do servidor.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "201", description = "Reprodução registrada", content = @Content(schema = @Schema(implementation = DadosListagemReproducao.class)))
    @ApiResponse(responseCode = "404", description = "Música não encontrada")
    public ResponseEntity<DadosListagemReproducao> cadastrar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(value = "{\"musicaId\":1}"))
            )
            @RequestBody @Valid DadosCadastroReproducao dados,
                                       @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        Usuario usuario = obterUsuarioAutenticado(usuarioAutenticado);
        var musica = musicaRepositorio.findById(dados.musicaId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Música não encontrada"));

        Reproducao r = reproducaoRepositorio.save(new Reproducao(usuario, musica));
        Long id = r.getId();
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(uri).body(new DadosListagemReproducao(r));
    }

    @GetMapping("/historico")
    @Operation(summary = "Consulta meu histórico", description = "Retorna somente o histórico do usuário autenticado, do mais recente ao mais antigo e de forma paginada.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<DadosListagemReproducao>> listar(@AuthenticationPrincipal UserDetails usuarioAutenticado,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {
        Usuario usuario = obterUsuarioAutenticado(usuarioAutenticado);
        int paginaSegura = Math.max(page, 0);
        int tamanhoSeguro = Math.min(Math.max(size, 1), 50);
        var historico = reproducaoRepositorio
                .findAllByUsuario_IdOrderByReproduzidaEmDescIdDesc(usuario.getId(), PageRequest.of(paginaSegura, tamanhoSeguro))
                .map(DadosListagemReproducao::new);
        return ResponseEntity.ok(historico);
    }

    private Usuario obterUsuarioAutenticado(UserDetails usuarioAutenticado) {
        return usuarioRepositorio.findByEmailIgnoreCase(usuarioAutenticado.getUsername())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuário não encontrado"));
    }
}
