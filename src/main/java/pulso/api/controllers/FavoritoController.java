package pulso.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pulso.api.favoritos.*;
import pulso.api.infra.ConflitoException;
import pulso.api.infra.RegistroNaoEncontradoException;
import pulso.api.musicas.MusicaRepositorio;
import pulso.api.usuarios.Usuario;
import pulso.api.usuarios.UsuarioRepositorio;

import java.net.URI;

@RestController
@RequestMapping("/favoritos")
@Tag(name = "Favoritos", description = "Músicas favoritas do usuário autenticado.")
public class FavoritoController {
    @Autowired
    private FavoritoRepositorio favoritoRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private MusicaRepositorio musicaRepositorio;

    @PostMapping("/{musicaId}")
    @Transactional
    @Operation(summary = "Favorita música", description = "Inclui a música indicada nos favoritos do usuário do JWT.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "201", description = "Favorito criado", content = @Content(schema = @Schema(implementation = DadosListagemFavorito.class)))
    @ApiResponse(responseCode = "404", description = "Música não encontrada")
    @ApiResponse(responseCode = "409", description = "Música já favoritada")
    public ResponseEntity<DadosListagemFavorito> cadastrar(@PathVariable Long musicaId,
                                                            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        Usuario usuario = obterUsuarioAutenticado(usuarioAutenticado);
        var musica = musicaRepositorio.findById(musicaId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Música não encontrada"));

        if (favoritoRepositorio.existsByUsuario_IdAndMusica_Id(usuario.getId(), musicaId)) {
            throw new ConflitoException("A música já está nos favoritos deste usuário");
        }

        Favorito f = favoritoRepositorio.save(new Favorito(usuario, musica));
        Long id = f.getId();
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(uri).body(new DadosListagemFavorito(f));
    }

    @GetMapping
    @Operation(summary = "Lista meus favoritos", description = "Retorna somente favoritos do usuário autenticado, de forma paginada.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<DadosListagemFavorito>> listar(@AuthenticationPrincipal UserDetails usuarioAutenticado,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "20") int size) {
        Usuario usuario = obterUsuarioAutenticado(usuarioAutenticado);
        int paginaSegura = Math.max(page, 0);
        int tamanhoSeguro = Math.min(Math.max(size, 1), 50);
        var favoritos = favoritoRepositorio.findAllByUsuario_Id(
                usuario.getId(), PageRequest.of(paginaSegura, tamanhoSeguro)
        ).map(DadosListagemFavorito::new);
        return ResponseEntity.ok(favoritos);
    }

    @DeleteMapping("/{musicaId}")
    @Transactional
    @Operation(summary = "Desfavorita música")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "204", description = "Favorito removido")
    @ApiResponse(responseCode = "404", description = "Música não está nos favoritos")
    public ResponseEntity<?> excluir(@PathVariable Long musicaId,
                                     @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        Usuario usuario = obterUsuarioAutenticado(usuarioAutenticado);
        Favorito favorito = favoritoRepositorio.findByUsuario_IdAndMusica_Id(usuario.getId(), musicaId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Música não está nos favoritos"));
        favoritoRepositorio.delete(favorito);
        return ResponseEntity.noContent().build();
    }

    private Usuario obterUsuarioAutenticado(UserDetails usuarioAutenticado) {
        return usuarioRepositorio.findByEmailIgnoreCase(usuarioAutenticado.getUsername())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuário não encontrado"));
    }
}
