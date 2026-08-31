package pulso.api.artistas;

import jakarta.persistence.*;
import lombok.*;
import pulso.api.albuns.Album;

import java.util.ArrayList;
import java.util.List;

@Table(name = "artista")
@Entity(name = "artistas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Artista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String biografia;
    private String imagemUrl;

    @OneToMany(mappedBy = "artista")
    private List<Album> albuns = new ArrayList<>();

    public Artista(DadosCadastroArtista dados) {
        this.nome = dados.nome();
        this.biografia = dados.biografia();
        this.imagemUrl = dados.imagemUrl();
    }

    public void atualizaInformacoes(DadosAlteracaoArtista dados) {
        if (dados.nome() != null) this.nome = dados.nome();
        if (dados.biografia() != null) this.biografia = dados.biografia();
        if (dados.imagemUrl() != null) this.imagemUrl = dados.imagemUrl();
    }
}
