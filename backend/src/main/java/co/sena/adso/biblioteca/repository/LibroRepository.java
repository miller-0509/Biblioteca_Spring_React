package co.sena.adso.biblioteca.repository;

import co.sena.adso.biblioteca.entity.EstadoLibro;
import co.sena.adso.biblioteca.entity.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    boolean existsByCodigoUnico(String codigoUnico);
    Optional<Libro> findByCodigoUnico(String codigoUnico);
    Page<Libro> findByEliminadoFalse(Pageable pageable);
    List<Libro> findByEliminadoFalse();
    Page<Libro> findByEliminadoFalseAndEstado(EstadoLibro estado, Pageable pageable);
    List<Libro> findByEliminadoFalseAndEstado(EstadoLibro estado);
    List<Libro> findByEliminadoFalseAndTituloContainingIgnoreCase(String titulo);
    long countByEliminadoFalse();
    long countByEstado(EstadoLibro estado);
    List<Libro> findByTituloContainingIgnoreCase(String titulo);
}
