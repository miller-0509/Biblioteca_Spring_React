package co.sena.adso.biblioteca.security;

import co.sena.adso.biblioteca.entity.Usuario;
import co.sena.adso.biblioteca.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utilidades para obtener el usuario autenticado desde el contexto de seguridad.
 */
@Component
public class SecurityService {

    private final UsuarioRepository usuarioRepository;

    public SecurityService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public CurrentUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CurrentUser currentUser) {
            return currentUser;
        }
        return null;
    }

    public Long getCurrentUserId() {
        CurrentUser currentUser = getCurrentUser();
        return currentUser != null ? currentUser.id() : null;
    }

    /**
     * Retorna la entidad Usuario del autenticado (recargada desde BD).
     */
    public Usuario getCurrentUsuario() {
        Long id = getCurrentUserId();
        if (id == null) {
            return null;
        }
        return usuarioRepository.findById(id).orElse(null);
    }
}
