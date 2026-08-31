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
import pulso.api.albuns.AlbumRepositorio;
import pulso.api.artistas.*;
import pulso.api.infra.ConflitoException;
import pulso.api.infra.RegistroNaoEncontradoException;

import java.net.URI;

@RestController
@RequestMapping("/artistas")
@Tag(name = "Catálogo - Artistas", description = "Consulta e manutenção de artistas do catálogo.")
public class ArtistaController {
    @Autowired
    private ArtistaRepositorio artistaRepositorio;

    @Autowired
    private AlbumRepositorio albumRepositorio;

    @PostMapping
    @Transactional
    @Operation(summary = "Cadastra artista")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "201", description = "Artista cadastrado", content = @Content(schema = @Schema(implementation = DadosListagemArtista.class)))
    @ApiResponse(responseCode = "401", description = "Token ausente ou inválido")
    public ResponseEntity<DadosListagemArtista> cadastrar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(
                            value = "{\"nome\":\"Milton Nascimento\",\"biografia\":\"Cantor e compositor brasileiro.\",\"imagemUrl\":\"https://exemplo.com/milton.jpg\"}"
                    ))
            )
            @RequestBody @Valid DadosCadastroArtista dados
    ) {
        Artista a = artistaRepositorio.save(new Artista(dados));
        Long id = a.getId();
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri().path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(uri).body(new DadosListagemArtista(a));
    }

    @GetMapping
    @Operation(summary = "Lista artistas", description = "Consulta pública paginada do catálogo de artistas.")
    public ResponseEntity<Page<DadosListagemArtista>> listar(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        int paginaSegura = Math.max(page, 0);
        int tamanhoSeguro = Math.min(Math.max(size, 1), 50);
        var artistas = artistaRepositorio.findAll(PageRequest.of(paginaSegura, tamanhoSeguro))
                .map(DadosListagemArtista::new);
        return ResponseEntity.ok(artistas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha artista", description = "Consulta pública de um artista pelo identificador.")
    @ApiResponse(responseCode = "404", description = "Artista não encontrado")
    public ResponseEntity<DadosListagemArtista> detalhar(@PathVariable Long id) {
        Artista artista = artistaRepositorio.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Artista não encontrado"));
        return ResponseEntity.ok(new DadosListagemArtista(artista));
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "Atualiza artista")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Artista atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Artista não encontrado")
    public ResponseEntity<DadosAlteracaoArtista> alterar(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(
                            value = "{\"biografia\":\"Biografia atualizada.\"}"
                    ))
            )
            @RequestBody @Valid DadosAlteracaoArtista dados
    ) {
        Artista a = artistaRepositorio.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Artista não encontrado"));
        a.atualizaInformacoes(dados);
        return ResponseEntity.ok(dados);
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "Exclui artista", description = "Só permite excluir artista sem álbuns vinculados.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "204", description = "Artista excluído")
    @ApiResponse(responseCode = "409", description = "Artista possui álbuns")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        Artista artista = artistaRepositorio.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Artista não encontrado"));

        if (albumRepositorio.existsByArtistaId(id)) {
            throw new ConflitoException("Não é possível excluir artista que possui álbuns");
        }

        artistaRepositorio.delete(artista);
        return ResponseEntity.noContent().build(); } }
