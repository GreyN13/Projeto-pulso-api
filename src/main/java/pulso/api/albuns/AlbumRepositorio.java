package pulso.api.albuns;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepositorio extends JpaRepository<Album,Long> {
    Page<Album> findAllByArtista_Id(Long artistaId, Pageable pageable);

    boolean existsByArtistaId(Long artistaId);
}
