package co.sena.adso.biblioteca.repository;

import co.sena.adso.biblioteca.entity.HistorialEstadoLibro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistorialEstadoLibroRepository extends JpaRepository<HistorialEstadoLibro, Long> {
    List<HistorialEstadoLibro> findByLibroId(Long idLibro);
    List<HistorialEstadoLibro> findByAdministradorId(Long idAdministrador);
}
