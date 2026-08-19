package co.sena.adso.biblioteca.repository;

import co.sena.adso.biblioteca.entity.HistorialEstadoEquipo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistorialEstadoEquipoRepository extends JpaRepository<HistorialEstadoEquipo, Long> {
    List<HistorialEstadoEquipo> findByEquipoId(Long idEquipo);
    List<HistorialEstadoEquipo> findByAdministradorId(Long idAdministrador);
}
