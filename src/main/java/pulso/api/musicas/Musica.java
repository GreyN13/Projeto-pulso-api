package pulso.api.musicas;

import jakarta.persistence.*;
import lombok.*;
import pulso.api.albuns.Album;
import pulso.api.playlists.Playlist;

import java.util.ArrayList;
import java.util.List;

@Table(name = "musica")
@Entity(name = "musicas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Musica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "album_id", insertable = false, updatable = false)
    private Long albumId;
    private String titulo;
    private String arquivoUrl;
    private int duracaoSegundos;

    @ManyToOne
    @JoinColumn(name = "album_id")
    private Album album;

    @ManyToMany(mappedBy = "musicas")
    private List<Playlist> playlists = new ArrayList<>();

    public Musica(DadosCadastroMusica dados, Album album) {
        this.titulo = dados.titulo();
        this.arquivoUrl = dados.arquivoUrl();
        this.duracaoSegundos = dados.duracaoSegundos();
        this.album = album;
    }

    public void atualizaInformacoes(DadosAlteracaoMusica dados) {
        if (dados.titulo() != null) this.titulo = dados.titulo();
        if (dados.arquivoUrl() != null) this.arquivoUrl = dados.arquivoUrl();
        if (dados.duracaoSegundos() != null) this.duracaoSegundos = dados.duracaoSegundos();
    }
}
