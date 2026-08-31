package pulso.api.musicas;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MusicaRepositorio extends JpaRepository<Musica, Long> {
    boolean existsByAlbumId(Long albumId);

    Page<Musica> findByTituloContainingIgnoreCaseOrAlbum_Artista_NomeContainingIgnoreCase(
            String titulo, String nomeArtista, Pageable pageable
    );
}
