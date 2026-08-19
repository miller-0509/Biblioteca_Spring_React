package co.sena.adso.biblioteca.repository;

import co.sena.adso.biblioteca.entity.RenovacionLibro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RenovacionLibroRepository extends JpaRepository<RenovacionLibro, Long> {
    List<RenovacionLibro> findByPrestamoLibroId(Long idPrestamoLibro);
    List<RenovacionLibro> findByUsuarioId(Long idUsuario);
    List<RenovacionLibro> findByAdministradorId(Long idAdministrador);
    List<RenovacionLibro> findByPrestamoLibroIdIn(Collection<Long> idPrestamoLibro);
    Optional<RenovacionLibro> findTopByPrestamoLibroIdAndEstadoOrderByFechaSolicitudDesc(Long idPrestamoLibro, String estado);
}
