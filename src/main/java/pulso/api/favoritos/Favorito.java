package pulso.api.favoritos;

import jakarta.persistence.*;
import lombok.*;
import pulso.api.musicas.Musica;
import pulso.api.usuarios.Usuario;

import java.util.Date;

@Table(name = "favorito")
@Entity(name = "favoritos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Favorito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "usuario_id", insertable = false, updatable = false)
    private Long usuarioId;

    @Column(name = "musica_id", insertable = false, updatable = false)
    private Long musicaId;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "criado_em", nullable = false, updatable = false)
    private Date criadoEm;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "musica_id")
    private Musica musica;

    public Favorito(Usuario usuario, Musica musica) {
        this.usuario = usuario;
        this.musica = musica;
    }

    @PrePersist
    private void definirCriadoEm() {
        this.criadoEm = new Date();
    }
}
