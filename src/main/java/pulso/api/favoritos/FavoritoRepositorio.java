package pulso.api.favoritos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoritoRepositorio extends JpaRepository<Favorito,Long> {
    boolean existsByUsuario_IdAndMusica_Id(Long usuarioId, Long musicaId);

    Optional<Favorito> findByUsuario_IdAndMusica_Id(Long usuarioId, Long musicaId);

    Page<Favorito> findAllByUsuario_Id(Long usuarioId, Pageable pageable);
}
