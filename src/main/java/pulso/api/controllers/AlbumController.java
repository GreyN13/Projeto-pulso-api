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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pulso.api.albuns.*;
import pulso.api.artistas.ArtistaRepositorio;
import pulso.api.infra.ConflitoException;
import pulso.api.infra.RegistroNaoEncontradoException;
import pulso.api.musicas.MusicaRepositorio;

import java.net.URI;

@RestController
@RequestMapping("/albuns")
@Tag(name = "Catálogo - Álbuns", description = "Consulta e manutenção dos álbuns do catálogo.")
public class AlbumController {
    @Autowired
    private AlbumRepositorio albumRepositorio;

    @Autowired
    private ArtistaRepositorio artistaRepositorio;

    @Autowired
    private MusicaRepositorio musicaRepositorio;

    @PostMapping
    @Transactional
    @Operation(summary = "Cadastra álbum")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "201", description = "Álbum cadastrado", content = @Content(schema = @Schema(implementation = DadosListagemAlbum.class)))
    @ApiResponse(responseCode = "404", description = "Artista não encontrado")
    public ResponseEntity<DadosListagemAlbum> cadastrar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(
                            value = "{\"titulo\":\"Clube da Esquina\",\"capaUrl\":\"https://exemplo.com/capa.jpg\",\"ano\":1972,\"artistaId\":1}"
                    ))
            )
            @RequestBody @Valid DadosCadastroAlbum dados
    ) {
        var artista = artistaRepositorio.findById(dados.artistaId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Artista não encontrado"));

        Album a = albumRepositorio.save(new Album(dados, artista));
        Long id = a.getId();
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(uri).body(new DadosListagemAlbum(a));
    }

    @GetMapping
    @Operation(summary = "Lista álbuns", description = "Consulta pública paginada; aceita o filtro opcional artistaId.")
    public ResponseEntity<Page<DadosListagemAlbum>> listar(@RequestParam(required = false) Long artistaId,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "20") int size) {
        int paginaSegura = Math.max(page, 0);
        int tamanhoSeguro = Math.min(Math.max(size, 1), 50);
        PageRequest pageable = PageRequest.of(paginaSegura, tamanhoSeguro);
        var albuns = artistaId == null
                ? albumRepositorio.findAll(pageable)
                : albumRepositorio.findAllByArtista_Id(artistaId, pageable);
        return ResponseEntity.ok(albuns.map(DadosListagemAlbum::new));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha álbum", description = "Consulta pública de um álbum pelo identificador.")
    @ApiResponse(responseCode = "404", description = "Álbum não encontrado")
    public ResponseEntity<DadosListagemAlbum> detalhar(@PathVariable Long id) {
        Album album = albumRepositorio.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Álbum não encontrado"));
        return ResponseEntity.ok(new DadosListagemAlbum(album));
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "Atualiza álbum")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Álbum atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Álbum ou artista não encontrado")
    public ResponseEntity<DadosListagemAlbum> alterar(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(value = "{\"titulo\":\"Novo título\",\"ano\":1973}"))
            )
            @RequestBody @Valid DadosAlteracaoAlbum dados
    ) {
        Album album = albumRepositorio.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Álbum não encontrado"));

        album.atualizaInformacoes(dados);
        if (dados.artistaId() != null) {
            var artista = artistaRepositorio.findById(dados.artistaId())
                    .orElseThrow(() -> new RegistroNaoEncontradoException("Artista não encontrado"));
            album.setArtista(artista);
        }

        return ResponseEntity.ok(new DadosListagemAlbum(album));
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "Exclui álbum", description = "Só permite excluir álbum sem músicas vinculadas.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "204", description = "Álbum excluído")
    @ApiResponse(responseCode = "409", description = "Álbum possui músicas")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        Album album = albumRepositorio.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Álbum não encontrado"));

        if (musicaRepositorio.existsByAlbumId(id)) {
            throw new ConflitoException("Não é possível excluir álbum que possui músicas");
        }

        albumRepositorio.delete(album);
        return ResponseEntity.noContent().build();
    }
}
