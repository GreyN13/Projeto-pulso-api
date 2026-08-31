package pulso.api.albuns;

import jakarta.persistence.*;
import lombok.*;
import pulso.api.artistas.Artista;
import pulso.api.musicas.Musica;

import java.util.ArrayList;
import java.util.List;

@Table(name = "album")
@Entity(name = "albuns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Album {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titulo;
    private String capaUrl;
    private int ano;

  //o objeto artista já existe, o controller já vai receber o id de lá, mas no projeto 
  // tá pedindo artistaIid como obrigatório, ele tecnicamente tá no obj artista q vai salvar a FK, mas
  //nomear ele assim parece errado, então fica só pra leitura o artistaID
  // o padrao segue em Musica, Playlist,Favorito e reproducao, todas essas classes sendo model
    @Column(name = "artista_id", insertable = false, updatable = false)
    private Long artistaId;

    @ManyToOne
    @JoinColumn(name = "artista_id")
    private Artista artista;

    @OneToMany(mappedBy = "album")
    private List<Musica> musicas = new ArrayList<>();

    public Album(DadosCadastroAlbum dados, Artista artista) {
        this.titulo = dados.titulo();
        this.capaUrl = dados.capaUrl();
        this.ano = dados.ano();
        this.artista = artista;
    }

    public void atualizaInformacoes(DadosAlteracaoAlbum dados) {
        if (dados.titulo() != null) this.titulo = dados.titulo();
        if (dados.capaUrl() != null) this.capaUrl = dados.capaUrl();
        if (dados.ano() != null) this.ano = dados.ano();
    }
}
