package co.sena.adso.biblioteca.config;

import co.sena.adso.biblioteca.entity.EstadoUsuario;
import co.sena.adso.biblioteca.entity.Usuario;
import co.sena.adso.biblioteca.repository.UsuarioRepository;
import co.sena.adso.biblioteca.security.CurrentUser;
import co.sena.adso.biblioteca.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectProvider<UsuarioRepository> usuarioRepositoryProvider;

    public JwtAuthenticationFilter(JwtService jwtService, ObjectProvider<UsuarioRepository> usuarioRepositoryProvider) {
        this.jwtService = jwtService;
        this.usuarioRepositoryProvider = usuarioRepositoryProvider;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        final String email = jwtService.extractUsername(token);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtService.isTokenValid(token, email)) {
                UsuarioRepository usuarioRepository = usuarioRepositoryProvider.getIfAvailable();
                if (usuarioRepository == null) {
                    filterChain.doFilter(request, response);
                    return;
                }
                Usuario usuario = usuarioRepository.findByCorreo(email).orElse(null);
                if (usuario != null && usuario.getEstado() == EstadoUsuario.activo) {
                    CurrentUser principal = new CurrentUser(usuario.getId(), usuario.getCorreo(), usuario.getRol());
                    UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()))
                        );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
