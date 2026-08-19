package co.sena.adso.biblioteca.repository;

import co.sena.adso.biblioteca.entity.TipoToken;
import co.sena.adso.biblioteca.entity.TokenVerificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TokenVerificacionRepository extends JpaRepository<TokenVerificacion, Long> {
    Optional<TokenVerificacion> findByToken(String token);
    Optional<TokenVerificacion> findTopByCorreoAndTipoOrderByCreadoEnDesc(String correo, TipoToken tipo);
}
