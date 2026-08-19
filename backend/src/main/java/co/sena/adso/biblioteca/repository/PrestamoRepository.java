package co.sena.adso.biblioteca.repository;

import co.sena.adso.biblioteca.entity.EstadoPrestamo;
import co.sena.adso.biblioteca.entity.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    List<Prestamo> findByUsuarioId(Long idUsuario);
    List<Prestamo> findByEquipoId(Long idEquipo);
    List<Prestamo> findByAdministradorId(Long idAdministrador);
    List<Prestamo> findByEstado(EstadoPrestamo estado);
    List<Prestamo> findByUsuarioIdAndEstadoIn(Long idUsuario, Collection<EstadoPrestamo> estados);
    List<Prestamo> findByEstadoAndFechaDevolucionEsperadaLessThan(EstadoPrestamo estado, LocalDateTime fecha);
    List<Prestamo> findByEstadoOrderByFechaSolicitudDesc(EstadoPrestamo estado);
    boolean existsByEquipoIdAndEstadoIn(Long idEquipo, Collection<EstadoPrestamo> estados);
    long countByUsuarioIdAndEstadoIn(Long idUsuario, Collection<EstadoPrestamo> estados);
}
