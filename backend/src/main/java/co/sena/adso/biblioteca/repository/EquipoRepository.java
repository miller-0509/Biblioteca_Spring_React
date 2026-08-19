package co.sena.adso.biblioteca.repository;

import co.sena.adso.biblioteca.entity.EstadoEquipo;
import co.sena.adso.biblioteca.entity.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    boolean existsByNumeroSerie(String numeroSerie);
    Optional<Equipo> findByNumeroSerie(String numeroSerie);
    List<Equipo> findByEliminadoFalse();
    List<Equipo> findByEliminadoFalseAndEstado(EstadoEquipo estado);
    List<Equipo> findByEliminadoFalseAndEstadoAndDisponiblePrestamoTrue(EstadoEquipo estado);
    List<Equipo> findByEliminadoFalseAndNombreContainingIgnoreCase(String nombre);
    List<Equipo> findByEliminadoFalseAndTipoEquipoIgnoreCase(String tipoEquipo);
    long countByEliminadoFalse();
    long countByEstado(EstadoEquipo estado);
}
