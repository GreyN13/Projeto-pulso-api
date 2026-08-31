package pulso.api.playlists;

import jakarta.persistence.*;
import lombok.*;
import pulso.api.albuns.Album;
import pulso.api.musicas.Musica;
import pulso.api.usuarios.Usuario;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Table(name = "playlist")
@Entity(name = "playlists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "usuario_id", insertable = false, updatable = false)
    private Long usuarioId;
    private String nome;
    private String descricao;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "criada_em", nullable = false, updatable = false)
    private Date criadaEm;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToMany
    @JoinTable(
            name = "playlist_musica",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "musica_id")
    )
    private List<Musica> musicas = new ArrayList<>();


    public Playlist(DadosCadastroPlaylist dados, Usuario usuario) {
        this.nome = dados.nome();
        this.descricao = dados.descricao();
        this.usuario = usuario;
    }

    @PrePersist
    private void definirCriadaEm() {
        this.criadaEm = new Date();
    }

    public void atualizaInformacoes(DadosAlteracaoPlaylist dados) {
        if (dados.nome() != null) this.nome = dados.nome();
        if (dados.descricao() != null) this.descricao = dados.descricao();
    }
}
