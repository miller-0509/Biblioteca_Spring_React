package co.sena.adso.biblioteca.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final ObjectMapper objectMapper;

    @Value("${app.security.enabled:true}")
    private boolean securityEnabled;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, ObjectMapper objectMapper) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    objectMapper.writeValue(response.getWriter(), Map.of(
                        "timestamp", LocalDateTime.now().toString(),
                        "status", 401,
                        "error", "Unauthorized",
                        "message", "Debes iniciar sesión para acceder a este recurso."
                    ));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    objectMapper.writeValue(response.getWriter(), Map.of(
                        "timestamp", LocalDateTime.now().toString(),
                        "status", 403,
                        "error", "Forbidden",
                        "message", "No tienes permisos para realizar esta acción."
                    ));
                })
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        if (securityEnabled) {
            http.authorizeHttpRequests(auth -> auth
                // ── Público ───────────────────────────────────────────
                .requestMatchers(
                    "/",
                    "/error",
                    "/api/auth/login",
                    "/api/auth/registro",
                    "/api/auth/verificar/**",
                    "/api/auth/reenviar-verificacion",
                    "/api/auth/recuperar-password",
                    "/api/auth/restablecer-password/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/actuator/health"
                ).permitAll()
                // ── Usuarios: historial propio (autenticado) o admin ──
                .requestMatchers(HttpMethod.GET, "/api/usuarios/*/historial").authenticated()
                // ── Usuarios: solo administrador ──────────────────────
                .requestMatchers("/api/usuarios/**").hasRole("administrador")
                // ── Catálogo (lectura para todos los autenticados) ────
                .requestMatchers(HttpMethod.GET, "/api/libros", "/api/libros/*").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/equipos", "/api/equipos/*").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/historial-estado-libros/*").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/historial-estado-equipos/*").authenticated()
                // ── Escritura de libros/equipos: roles de gestión ─────
                .requestMatchers("/api/libros/**").hasAnyRole("administrador", "bibliotecario")
                .requestMatchers("/api/equipos/**").hasAnyRole("administrador", "almacenista")
                // ── Préstamos de libros ───────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/prestamos-libros/usuario/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/prestamos-libros/libro/**").hasAnyRole("administrador", "bibliotecario")
                .requestMatchers(HttpMethod.GET, "/api/prestamos-libros/*").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/prestamos-libros").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/prestamos-libros").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/prestamos-libros/*/renovar").authenticated()
                .requestMatchers("/api/prestamos-libros/**").hasAnyRole("administrador", "bibliotecario")
                // ── Préstamos de equipos ───────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/prestamos/usuario/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/prestamos/equipo/**").hasAnyRole("administrador", "almacenista")
                .requestMatchers(HttpMethod.GET, "/api/prestamos/*").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/prestamos").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/prestamos").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/prestamos/*/renovar").authenticated()
                .requestMatchers("/api/prestamos/**").hasAnyRole("administrador", "almacenista")
                // ── Renovaciones ───────────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/renovaciones-libros/**").authenticated()
                .requestMatchers("/api/renovaciones-libros/**").hasAnyRole("administrador", "bibliotecario")
                .requestMatchers(HttpMethod.GET, "/api/renovaciones-equipos/**").authenticated()
                .requestMatchers("/api/renovaciones-equipos/**").hasAnyRole("administrador", "almacenista")
                // ── Historial: cambios de estado requieren gestión ────
                .requestMatchers("/api/historial-estado-libros/**").hasAnyRole("administrador", "bibliotecario")
                .requestMatchers("/api/historial-estado-equipos/**").hasAnyRole("administrador", "almacenista")
                // ── Multas: consulta para todos, condonación/gestión para staff ──
                .requestMatchers(HttpMethod.GET, "/api/multas/**").authenticated()
                .requestMatchers("/api/multas/**").hasAnyRole("administrador", "bibliotecario", "almacenista")
                // ── Reportes / Dashboard ───────────────────────────────
                .requestMatchers("/api/reportes/**", "/api/dashboard/**").authenticated()
                // ── Resto autenticado ──────────────────────────────────
                .anyRequest().authenticated());
        } else {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        }
        return http.build();
    }
}
