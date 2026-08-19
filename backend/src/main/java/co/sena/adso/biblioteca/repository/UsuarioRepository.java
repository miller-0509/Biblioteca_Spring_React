package co.sena.adso.biblioteca.repository;

import co.sena.adso.biblioteca.entity.EstadoUsuario;
import co.sena.adso.biblioteca.entity.RolUsuario;
import co.sena.adso.biblioteca.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
    List<Usuario> findByRol(RolUsuario rol);
    List<Usuario> findByEstado(EstadoUsuario estado);
    List<Usuario> findByRolAndEstado(RolUsuario rol, EstadoUsuario estado);
    long countByRol(RolUsuario rol);
    long countByEstado(EstadoUsuario estado);
    long countByRolAndEstado(RolUsuario rol, EstadoUsuario estado);
}
