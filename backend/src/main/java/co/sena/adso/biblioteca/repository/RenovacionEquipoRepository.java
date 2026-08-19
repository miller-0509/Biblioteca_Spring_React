package co.sena.adso.biblioteca.repository;

import co.sena.adso.biblioteca.entity.RenovacionEquipo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RenovacionEquipoRepository extends JpaRepository<RenovacionEquipo, Long> {
    List<RenovacionEquipo> findByPrestamoId(Long idPrestamo);
    List<RenovacionEquipo> findByUsuarioId(Long idUsuario);
    List<RenovacionEquipo> findByAdministradorId(Long idAdministrador);
    List<RenovacionEquipo> findByPrestamoIdIn(Collection<Long> idPrestamo);
    Optional<RenovacionEquipo> findTopByPrestamoIdAndEstadoOrderByFechaSolicitudDesc(Long idPrestamo, String estado);
}
