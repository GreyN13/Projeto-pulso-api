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
import pulso.api.musicas.*;
import pulso.api.albuns.AlbumRepositorio;
import pulso.api.infra.RegistroNaoEncontradoException;

import java.net.URI;

@RestController
@RequestMapping("/musicas")
@Tag(name = "Catálogo - Músicas", description = "Consulta, busca e manutenção das músicas do catálogo.")
public class MusicaController {
    @Autowired
    private MusicaRepositorio musicaRepositorio;

    @Autowired
    private AlbumRepositorio albumRepositorio;

    @PostMapping
    @Transactional
    @Operation(summary = "Cadastra música")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "201", description = "Música cadastrada", content = @Content(schema = @Schema(implementation = DadosListagemMusica.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Álbum não encontrado")
    public ResponseEntity<DadosListagemMusica> cadastrar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(
                            value = "{\"titulo\":\"Cais\",\"duracaoSegundos\":182,\"arquivoUrl\":\"https://exemplo.com/cais.mp3\",\"albumId\":1}"
                    ))
            )
            @RequestBody @Valid DadosCadastroMusica dados
    ) {
        var album = albumRepositorio.findById(dados.albumId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Álbum não encontrado"));

        Musica m = musicaRepositorio.save(new Musica(dados, album));
        Long id = m.getId();
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(uri).body(new DadosListagemMusica(m));
    }

    @GetMapping
    @Operation(summary = "Lista ou busca músicas", description = "Consulta pública paginada. O parâmetro busca procura por título ou nome do artista, sem diferenciar maiúsculas de minúsculas.")
    public ResponseEntity<Page<DadosListagemMusica>> listar(@RequestParam(required = false) String busca,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size)

    //20 fica de valor padrão, 50 de valor máximo, realisticamente não sei quantos dados
    // simultanos pro client por pagina seriam uma margem segura, e assim fica padrao pra buscas similares nas outras
    // classes,já que foi pedido paginação.

    {
        int paginaSegura = Math.max(page, 0);
        int tamanhoSeguro = Math.min(Math.max(size, 1), 50);
        PageRequest pageable = PageRequest.of(paginaSegura, tamanhoSeguro);

        var musicas = busca == null || busca.isBlank()
                ? musicaRepositorio.findAll(pageable)
                : musicaRepositorio.findByTituloContainingIgnoreCaseOrAlbum_Artista_NomeContainingIgnoreCase(
                        busca, busca, pageable
                );

        return ResponseEntity.ok(musicas.map(DadosListagemMusica::new));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha música", description = "Consulta pública de uma música pelo identificador.")
    @ApiResponse(responseCode = "404", description = "Música não encontrada")
    public ResponseEntity<DadosListagemMusica> detalhar(@PathVariable Long id) {
        Musica musica = musicaRepositorio.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Música não encontrada"));
        return ResponseEntity.ok(new DadosListagemMusica(musica));
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "Atualiza música")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Música atualizada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Música ou álbum não encontrado")
    public ResponseEntity<DadosAlteracaoMusica> alterar(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(value = "{\"duracaoSegundos\":190}"))
            )
            @RequestBody @Valid DadosAlteracaoMusica dados
    ) {
        Musica m = musicaRepositorio.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Música não encontrada"));
        m.atualizaInformacoes(dados);
        if (dados.albumId() != null) {
            var album = albumRepositorio.findById(dados.albumId())
                    .orElseThrow(() -> new RegistroNaoEncontradoException("Álbum não encontrado"));
            m.setAlbum(album);
        }
        return ResponseEntity.ok(dados);
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "Exclui música")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "204", description = "Música excluída")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        Musica musica = musicaRepositorio.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Música não encontrada"));
        musicaRepositorio.delete(musica);
        return ResponseEntity.noContent().build();
    }
}
