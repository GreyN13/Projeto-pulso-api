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
import pulso.api.infra.AcessoNegadoException;
import pulso.api.infra.RegistroNaoEncontradoException;
import pulso.api.musicas.Musica;
import pulso.api.musicas.MusicaRepositorio;
import pulso.api.infra.ConflitoException;
import pulso.api.playlists.*;
import pulso.api.usuarios.Usuario;
import pulso.api.usuarios.UsuarioRepositorio;

import java.net.URI;

@RestController
@RequestMapping("/playlists")
@Tag(name = "Playlists", description = "Playlists particulares do usuário autenticado.")
public class PlaylistController {
    @Autowired
    private PlaylistRepositorio playlistRepositorio;

    @Autowired
    private MusicaRepositorio musicaRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @PostMapping
    @Transactional
    @Operation(summary = "Cria playlist", description = "A playlist é vinculada automaticamente ao usuário do JWT.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "201", description = "Playlist criada", content = @Content(schema = @Schema(implementation = DadosListagemPlaylist.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    public ResponseEntity<DadosListagemPlaylist> cadastrar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(
                            value = "{\"nome\":\"Favoritas para estudar\",\"descricao\":\"Seleção para concentração\"}"
                    ))
            )
            @RequestBody @Valid DadosCadastroPlaylist dados,
                                       @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        Usuario usuario = obterUsuarioAutenticado(usuarioAutenticado);

        Playlist p = playlistRepositorio.save(new Playlist(dados, usuario));
        Long id = p.getId();
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(uri).body(new DadosListagemPlaylist(p));
    }

    @GetMapping
    @Operation(summary = "Lista minhas playlists", description = "Retorna somente playlists do usuário autenticado, de forma paginada.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Page<DadosListagemPlaylist>> listar(@AuthenticationPrincipal UserDetails usuarioAutenticado,
                                                               @RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "20") int size) {
        Usuario usuario = obterUsuarioAutenticado(usuarioAutenticado);
        int paginaSegura = Math.max(page, 0);
        int tamanhoSeguro = Math.min(Math.max(size, 1), 50);
        var playlists = playlistRepositorio.findAllByUsuario_Id(
                usuario.getId(), PageRequest.of(paginaSegura, tamanhoSeguro)
        ).map(DadosListagemPlaylist::new);
        return ResponseEntity.ok(playlists);
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "Atualiza minha playlist")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Playlist atualizada")
    @ApiResponse(responseCode = "403", description = "Playlist pertence a outro usuário")
    public ResponseEntity<DadosAlteracaoPlaylist> alterar(
                                     @PathVariable Long id,
                                     @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                             required = true,
                                             content = @Content(examples = @ExampleObject(value = "{\"nome\":\"Novo nome\"}"))
                                     )
                                     @RequestBody @Valid DadosAlteracaoPlaylist dados,
                                     @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        Playlist playlist = obterPlaylist(id);

        Usuario usuario = obterUsuarioAutenticado(usuarioAutenticado);
        verificarDono(playlist, usuario);

        playlist.atualizaInformacoes(dados);
        return ResponseEntity.ok(dados);
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "Exclui minha playlist")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "204", description = "Playlist excluída")
    @ApiResponse(responseCode = "403", description = "Playlist pertence a outro usuário")
    public ResponseEntity<?> excluir(@PathVariable Long id,
                                     @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        Playlist playlist = obterPlaylist(id);

        Usuario usuario = obterUsuarioAutenticado(usuarioAutenticado);
        verificarDono(playlist, usuario);

        playlistRepositorio.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/musicas/{musicaId}")
    @Transactional
    @Operation(summary = "Adiciona música à minha playlist")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "201", description = "Música adicionada", content = @Content(schema = @Schema(implementation = DadosListagemPlaylist.class)))
    @ApiResponse(responseCode = "403", description = "Playlist pertence a outro usuário")
    @ApiResponse(responseCode = "409", description = "Música já pertence à playlist")
    public ResponseEntity<DadosListagemPlaylist> adicionarMusica(@PathVariable Long id, @PathVariable Long musicaId,
                                                                  @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        Playlist playlist = obterPlaylist(id);

        Usuario usuario = obterUsuarioAutenticado(usuarioAutenticado);
        verificarDono(playlist, usuario);

        Musica musica = musicaRepositorio.findById(musicaId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Música não encontrada"));

        boolean jaAdicionada = playlist.getMusicas().stream()
                .anyMatch(item -> item.getId().equals(musicaId));
        if (jaAdicionada) {
            throw new ConflitoException("A música já pertence a esta playlist");
        }

        playlist.getMusicas().add(musica);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri();
        return ResponseEntity.created(uri).body(new DadosListagemPlaylist(playlist));
    }

    @DeleteMapping("/{id}/musicas/{musicaId}")
    @Transactional
    @Operation(summary = "Remove música da minha playlist")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "204", description = "Música removida")
    @ApiResponse(responseCode = "403", description = "Playlist pertence a outro usuário")
    public ResponseEntity<?> removerMusica(@PathVariable Long id, @PathVariable Long musicaId,
                                           @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        Playlist playlist = obterPlaylist(id);

        Usuario usuario = obterUsuarioAutenticado(usuarioAutenticado);
        verificarDono(playlist, usuario);

        boolean removida = playlist.getMusicas().removeIf(musica -> musica.getId().equals(musicaId));
        if (!removida) {
            throw new RegistroNaoEncontradoException("Música não pertence a esta playlist");
        }

        return ResponseEntity.noContent().build();
    }

    private Usuario obterUsuarioAutenticado(UserDetails usuarioAutenticado) {
        return usuarioRepositorio.findByEmailIgnoreCase(usuarioAutenticado.getUsername())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuário não encontrado"));
    }

    private Playlist obterPlaylist(Long id) {
        return playlistRepositorio.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Playlist não encontrada"));
    }

    private void verificarDono(Playlist playlist, Usuario usuarioAutenticado) {
        if (!playlist.getUsuario().getId().equals(usuarioAutenticado.getId())) {
            throw new AcessoNegadoException("Você não pode alterar esta playlist");
        }
    }
}
