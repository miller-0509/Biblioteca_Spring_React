package co.sena.adso.biblioteca.repository;

import co.sena.adso.biblioteca.entity.EstadoPrestamoLibro;
import co.sena.adso.biblioteca.entity.PrestamoLibro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface PrestamoLibroRepository extends JpaRepository<PrestamoLibro, Long> {
    List<PrestamoLibro> findByUsuarioId(Long idUsuario);
    List<PrestamoLibro> findByLibroId(Long idLibro);
    List<PrestamoLibro> findByAdministradorId(Long idAdministrador);
    List<PrestamoLibro> findByEstado(EstadoPrestamoLibro estado);
    List<PrestamoLibro> findByUsuarioIdAndEstadoIn(Long idUsuario, Collection<EstadoPrestamoLibro> estados);
    List<PrestamoLibro> findByEstadoAndFechaDevolucionEsperadaLessThan(EstadoPrestamoLibro estado, LocalDateTime fecha);
    List<PrestamoLibro> findByEstadoOrderByFechaSolicitudDesc(EstadoPrestamoLibro estado);
    boolean existsByLibroIdAndEstadoIn(Long idLibro, Collection<EstadoPrestamoLibro> estados);
    long countByUsuarioIdAndEstadoIn(Long idUsuario, Collection<EstadoPrestamoLibro> estados);
}
