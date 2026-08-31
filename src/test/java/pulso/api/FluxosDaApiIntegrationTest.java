package pulso.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import pulso.api.albuns.AlbumRepositorio;
import pulso.api.artistas.ArtistaRepositorio;
import pulso.api.musicas.MusicaRepositorio;
import pulso.api.seguranca.LimitadorTentativasLogin;
import pulso.api.usuarios.UsuarioRepositorio;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FluxosDaApiIntegrationTest {
    private static final String SENHA = "senha-segura-123";
    private static final AtomicInteger PROXIMO_IP = new AtomicInteger(1);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private MusicaRepositorio musicaRepositorio;

    @Autowired
    private ArtistaRepositorio artistaRepositorio;

    @Autowired
    private AlbumRepositorio albumRepositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LimitadorTentativasLogin limitadorTentativasLogin;

    @BeforeEach
    void limparDadosGeradosNosTestes() {
        jdbcTemplate.execute("DELETE FROM favorito");
        jdbcTemplate.execute("DELETE FROM playlist_musica");
        jdbcTemplate.execute("DELETE FROM reproducao");
        jdbcTemplate.execute("DELETE FROM playlist");
        jdbcTemplate.execute("DELETE FROM usuario");
    }

    @Test
    void cadastraUsuarioComSenhaCriptografadaETrataValidacaoEDuplicidade() throws Exception {
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Ana\",\"email\":\"ana@pulso.test\",\"senha\":\"%s\"}".formatted(SENHA)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.email").value("ana@pulso.test"))
                .andExpect(jsonPath("$.senha").doesNotExist());

        var usuario = usuarioRepositorio.findByEmailIgnoreCase("ana@pulso.test").orElseThrow();
        assertThat(usuario.getSenha()).isNotEqualTo(SENHA);
        assertThat(passwordEncoder.matches(SENHA, usuario.getSenha())).isTrue();

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"ana@pulso.test\",\"senha\":\"%s\"}".formatted(SENHA)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"email\":\"invalido\",\"senha\":\"curta\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.campos").isArray());
    }

    @Test
    void autenticaComJwtERejeitaCredenciaisInvalidas() throws Exception {
        cadastrarUsuario("login@pulso.test");

        String token = autenticar("login@pulso.test", SENHA);
        assertThat(token).isNotBlank();

        mockMvc.perform(post("/auth")
                        .with(request -> {
                            request.setRemoteAddr(proximoIp());
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"login@pulso.test\",\"senha\":\"%s\"}".formatted(SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiraEm").exists())
                .andExpect(jsonPath("$.dataExpiracao").doesNotExist());

        mockMvc.perform(post("/auth")
                        .with(request -> {
                            request.setRemoteAddr(proximoIp());
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"login@pulso.test\",\"senha\":\"senha-incorreta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void retorna401QuandoRotaPrivadaRecebeTokenAusente() throws Exception {
        mockMvc.perform(post("/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Sem token\",\"descricao\":\"\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.erro").value("Token ausente ou inválido"));
    }

    @Test
    void deixaSwaggerAcessivelSemToken() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths").isMap())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.paths['/playlists'].post.security[0].bearerAuth").isArray());
    }

    @Test
    void impedeAlteracaoDePlaylistDeOutroUsuarioCom403() throws Exception {
        String tokenDono = cadastrarEAutenticar("dono@pulso.test");
        String tokenIntruso = cadastrarEAutenticar("intruso@pulso.test");
        long playlistId = criarPlaylist(tokenDono, "Playlist do dono");

        mockMvc.perform(put("/playlists/{id}", playlistId)
                        .header("Authorization", "Bearer " + tokenIntruso)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Tentativa indevida\",\"descricao\":\"\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void impedeAdicionarRemoverEExcluirPlaylistDeOutroUsuarioCom403() throws Exception {
        String tokenDono = cadastrarEAutenticar("dono-mutacoes@pulso.test");
        String tokenIntruso = cadastrarEAutenticar("intruso-mutacoes@pulso.test");
        long playlistId = criarPlaylist(tokenDono, "Playlist protegida");
        long musicaId = primeiraMusicaId();

        mockMvc.perform(post("/playlists/{id}/musicas/{musicaId}", playlistId, musicaId)
                        .header("Authorization", "Bearer " + tokenIntruso))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/playlists/{id}/musicas/{musicaId}", playlistId, musicaId)
                        .header("Authorization", "Bearer " + tokenIntruso))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/playlists/{id}", playlistId)
                        .header("Authorization", "Bearer " + tokenIntruso))
                .andExpect(status().isForbidden());
    }

    @Test
    void impedeMusicaDuplicadaNaMesmaPlaylistCom409() throws Exception {
        String token = cadastrarEAutenticar("playlist@pulso.test");
        long playlistId = criarPlaylist(token, "Favoritas");
        long musicaId = primeiraMusicaId();

        mockMvc.perform(post("/playlists/{id}/musicas/{musicaId}", playlistId, musicaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(playlistId));

        mockMvc.perform(post("/playlists/{id}/musicas/{musicaId}", playlistId, musicaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void impedeFavoritoDuplicadoCom409() throws Exception {
        String token = cadastrarEAutenticar("favorito@pulso.test");
        long musicaId = primeiraMusicaId();

        mockMvc.perform(post("/favoritos/{musicaId}", musicaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/favoritos/{musicaId}", musicaId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void retorna404QuandoAMusicaNaoExiste() throws Exception {
        String token = cadastrarEAutenticar("nao-encontrado@pulso.test");

        mockMvc.perform(post("/favoritos/{musicaId}", 999_999L)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void listaApenasDadosDoUsuarioAutenticado() throws Exception {
        String tokenDono = cadastrarEAutenticar("dados-dono@pulso.test");
        String tokenOutro = cadastrarEAutenticar("dados-outro@pulso.test");
        long musicaId = primeiraMusicaId();

        criarPlaylist(tokenDono, "Privada");
        mockMvc.perform(post("/favoritos/{musicaId}", musicaId)
                        .header("Authorization", "Bearer " + tokenDono))
                .andExpect(status().isCreated());
        registrarReproducao(tokenDono, musicaId);

        mockMvc.perform(get("/playlists").header("Authorization", "Bearer " + tokenOutro))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
        mockMvc.perform(get("/favoritos").header("Authorization", "Bearer " + tokenOutro))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
        mockMvc.perform(get("/reproducoes/historico").header("Authorization", "Bearer " + tokenOutro))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    void ordenaHistoricoDoMaisRecenteParaOMaisAntigo() throws Exception {
        String token = cadastrarEAutenticar("ordem@pulso.test");
        long primeiraMusica = musicaRepositorio.findAll().get(0).getId();
        long segundaMusica = musicaRepositorio.findAll().get(1).getId();

        registrarReproducao(token, primeiraMusica);
        Thread.sleep(10);
        registrarReproducao(token, segundaMusica);

        mockMvc.perform(get("/reproducoes/historico?page=0&size=20")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].musicaId").value(segundaMusica))
                .andExpect(jsonPath("$.content[1].musicaId").value(primeiraMusica));
    }

    @Test
    void retorna429DepoisDoLimiteDeTentativasDeLogin() throws Exception {
        String ip = "172.16.0.1";
        for (int tentativa = 0; tentativa < 67; tentativa++) {
            assertThat(limitadorTentativasLogin.tentativaPermitida(ip)).isTrue();
        }

        mockMvc.perform(post("/auth")
                        .with(request -> {
                            request.setRemoteAddr(ip);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ausente@pulso.test\",\"senha\":\"senha-invalida\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429));
    }

    @Test
    void impedeExcluirArtistaQuePossuiAlbumCom409() throws Exception {
        String token = cadastrarEAutenticar("artista@pulso.test");
        long artistaComAlbum = artistaRepositorio.findAll().stream()
                .map(artista -> artista.getId())
                .filter(albumRepositorio::existsByArtistaId)
                .findFirst()
                .orElseThrow();

        mockMvc.perform(delete("/artistas/{id}", artistaComAlbum)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void permiteDetalharAlterarEExcluirAlbumSemMusicas() throws Exception {
        String token = cadastrarEAutenticar("crud-album@pulso.test");
        long artistaId = criarArtista(token, "Artista temporário");
        long albumId = criarAlbum(token, artistaId, "Álbum temporário");

        mockMvc.perform(get("/artistas/{id}", artistaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(artistaId));
        mockMvc.perform(get("/albuns/{id}", albumId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(albumId));

        mockMvc.perform(put("/albuns/{id}", albumId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Álbum alterado\",\"ano\":2026}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Álbum alterado"));

        mockMvc.perform(delete("/albuns/{id}", albumId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void impedeExcluirAlbumQuePossuiMusicasCom409() throws Exception {
        String token = cadastrarEAutenticar("conflito-album@pulso.test");
        long albumComMusica = albumRepositorio.findAll().stream()
                .map(album -> album.getId())
                .filter(musicaRepositorio::existsByAlbumId)
                .findFirst()
                .orElseThrow();

        mockMvc.perform(delete("/albuns/{id}", albumComMusica)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void aplicaValidacoesTambemNosPuts() throws Exception {
        String token = cadastrarEAutenticar("validacao-put@pulso.test");
        long playlistId = criarPlaylist(token, "Playlist válida");
        long musicaId = primeiraMusicaId();

        mockMvc.perform(put("/playlists/{id}", playlistId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        mockMvc.perform(put("/musicas/{id}", musicaId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\",\"duracaoSegundos\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void paginaHistoricoDoUsuarioAutenticadoEListaCatalogoSemToken() throws Exception {
        String token = cadastrarEAutenticar("historico@pulso.test");
        long musicaId = primeiraMusicaId();

        mockMvc.perform(post("/reproducoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"musicaId\":%d}".formatted(musicaId)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/reproducoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"musicaId\":%d}".formatted(musicaId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/reproducoes/historico?page=0&size=1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(get("/musicas?busca=&page=0&size=500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(50));

        mockMvc.perform(get("/artistas?page=0&size=1"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/albuns?page=0&size=1"))
                .andExpect(status().isOk());
    }

    private void cadastrarUsuario(String email) throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Usuário de teste\",\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, SENHA)))
                .andExpect(status().isCreated());
    }

    private String cadastrarEAutenticar(String email) throws Exception {
        cadastrarUsuario(email);
        return autenticar(email, SENHA);
    }

    private String autenticar(String email, String senha) throws Exception {
        String resposta = mockMvc.perform(post("/auth")
                        .with(request -> {
                            request.setRemoteAddr(proximoIp());
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return resposta.replaceFirst(".*\\\"token\\\":\\\"([^\\\"]+)\\\".*", "$1");
    }

    private long criarPlaylist(String token, String nome) throws Exception {
        String localizacao = mockMvc.perform(post("/playlists")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"%s\",\"descricao\":\"Playlist de teste\"}".formatted(nome)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");
        return Long.parseLong(localizacao.substring(localizacao.lastIndexOf('/') + 1));
    }

    private long primeiraMusicaId() {
        return musicaRepositorio.findAll().stream().findFirst().orElseThrow().getId();
    }

    private long criarArtista(String token, String nome) throws Exception {
        String localizacao = mockMvc.perform(post("/artistas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"%s\",\"biografia\":\"\",\"imagemUrl\":\"\"}".formatted(nome)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");
        return Long.parseLong(localizacao.substring(localizacao.lastIndexOf('/') + 1));
    }

    private long criarAlbum(String token, long artistaId, String titulo) throws Exception {
        String localizacao = mockMvc.perform(post("/albuns")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"%s\",\"capaUrl\":\"\",\"ano\":2026,\"artistaId\":%d}"
                                .formatted(titulo, artistaId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");
        return Long.parseLong(localizacao.substring(localizacao.lastIndexOf('/') + 1));
    }

    private void registrarReproducao(String token, long musicaId) throws Exception {
        mockMvc.perform(post("/reproducoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"musicaId\":%d}".formatted(musicaId)))
                .andExpect(status().isCreated());
    }

    @Test
    void rejeitaReproducaoSemMusicaCom400() throws Exception {
        String token = cadastrarEAutenticar("reproducao-invalida@pulso.test");

        mockMvc.perform(post("/reproducoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.campos[0].campo").value("musicaId"));
    }

    private String proximoIp() {
        return "10.0.0." + PROXIMO_IP.getAndIncrement();
    }
}
