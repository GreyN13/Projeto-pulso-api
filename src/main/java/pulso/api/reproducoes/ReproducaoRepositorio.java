package pulso.api.reproducoes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReproducaoRepositorio extends JpaRepository<Reproducao,Long> {
    Page<Reproducao> findAllByUsuario_IdOrderByReproduzidaEmDescIdDesc(Long usuarioId, Pageable pageable);
}
