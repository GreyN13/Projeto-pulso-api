package pulso.api.reproducoes;

import jakarta.persistence.*;
import lombok.*;
import pulso.api.musicas.Musica;
import pulso.api.usuarios.Usuario;

import java.util.Date;

@Table(name = "reproducao")
@Entity(name = "reproducoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Reproducao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "usuario_id", insertable = false, updatable = false)
    private Long usuarioId;

    @Column(name = "musica_id", insertable = false, updatable = false)
    private Long musicaId;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "reproduzida_em", nullable = false, updatable = false)
    private Date reproduzidaEm;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "musica_id")
    private Musica musica;

    public Reproducao(Usuario usuario, Musica musica) {
        this.usuario = usuario;
        this.musica = musica;
    }

    @PrePersist
    private void definirReproduzidaEm() {
        this.reproduzidaEm = new Date();
    }
}
