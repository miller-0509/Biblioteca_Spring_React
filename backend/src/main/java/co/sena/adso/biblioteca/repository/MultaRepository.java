package co.sena.adso.biblioteca.repository;

import co.sena.adso.biblioteca.entity.EstadoMulta;
import co.sena.adso.biblioteca.entity.Multa;
import co.sena.adso.biblioteca.entity.TipoRecurso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MultaRepository extends JpaRepository<Multa, Long> {
    List<Multa> findByUsuarioId(Long idUsuario);
    Optional<Multa> findByPrestamoEquipoId(Long idPrestamoEquipo);
    Optional<Multa> findByPrestamoLibroId(Long idPrestamoLibro);
    List<Multa> findByEstado(EstadoMulta estado);
    List<Multa> findByEstadoAndFechaFinSuspensionLessThanEqual(EstadoMulta estado, LocalDateTime fecha);
    long countByUsuarioIdAndEstadoIn(Long idUsuario, Collection<EstadoMulta> estados);
    List<Multa> findByTipoRecursoOrderByFechaGeneracionDesc(TipoRecurso tipoRecurso);
    List<Multa> findAllByOrderByFechaGeneracionDesc();
    void deleteByUsuarioId(Long idUsuario);
    List<Multa> findByAdministradorResolucionId(Long idAdministradorResolucion);
}
