package pulso.api.usuarios;

import jakarta.persistence.*;
import lombok.*;
import pulso.api.favoritos.Favorito;
import pulso.api.playlists.Playlist;
import pulso.api.reproducoes.Reproducao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Table(name = "usuario", uniqueConstraints = @UniqueConstraint(name = "uk_usuario_email", columnNames = "email"))
@Entity(name="usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    @Column(nullable = false, unique = true)
    private String email;
    private String senha;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Date criadoEm;

    @OneToMany(mappedBy = "usuario")
    private List<Playlist> playlists = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private List<Favorito> favoritos = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private List<Reproducao> reproducoes = new ArrayList<>();

    public Usuario(DadosCadastroUsuario dados, String senhaCriptografada) {
        this.nome = dados.nome();
        this.email = dados.email();
        this.senha = senhaCriptografada;
    }
  
  //tirei os Records dos Dates pra deixar totalmente server side, vai q o client
  //envia que foi criado em 4 trilhoes antes de cristo, tentar forçar alguma bomba no banco
  //não sei como poderia ser explorado, mas é algo a se evitar, padrão para todas as datas
    @PrePersist
    private void definirCriadoEm() {
        this.criadoEm = new Date();
    }

    public void atualizaInformacoes(DadosAlteracaoUsuario dados) {
        if(dados.nome() != null) {
            this.nome = dados.nome();
        }
        if(dados.email() != null) {
            this.email = dados.email();
        }
    }
}

