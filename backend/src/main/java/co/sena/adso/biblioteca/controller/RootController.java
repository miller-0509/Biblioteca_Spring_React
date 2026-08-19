package co.sena.adso.biblioteca.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Tag(name = "Raíz", description = "Información general de la API")
public class RootController {

    @GetMapping("/")
    @Operation(summary = "Información de la API")
    public Map<String, Object> info() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nombre", "Biblioteca API — Spring Boot");
        body.put("version", "0.0.1-SNAPSHOT");
        body.put("descripcion", "API REST de gestión de biblioteca/almacén (libros, equipos, préstamos, renovaciones, multas, reportes).");
        body.put("swagger", "/swagger-ui.html");
        body.put("openapi", "/v3/api-docs");
        body.put("health", "/actuator/health");
        body.put("autenticacion", "POST /api/auth/login");
        body.put("endpoints", Map.of(
                "libros", "/api/libros",
                "equipos", "/api/equipos",
                "usuarios", "/api/usuarios",
                "prestamos-equipos", "/api/prestamos",
                "prestamos-libros", "/api/prestamos-libros",
                "multas", "/api/multas",
                "reportes", "/api/reportes"
        ));
        body.put("credenciales_de_prueba", Map.of(
                "administrador", Map.of("correo", "admin@email.com", "password", "Admin1234"),
                "bibliotecario", Map.of("correo", "carlos@email.com", "password", "123456"),
                "almacenista", Map.of("correo", "almacen@email.com", "password", "Clave1234"),
                "aprendiz", Map.of("correo", "maria@email.com", "password", "654321"),
                "instructor", Map.of("correo", "pedro@email.com", "password", "Clave1234")
        ));
        body.put("guia_de_revision", Map.of(
                "paso1", "Abrir en el navegador: /swagger-ui.html (documentación interactiva de todos los endpoints).",
                "paso2", "Iniciar sesión con POST /api/auth/login usando una de las credenciales_de_prueba; copiar el 'token' devuelto.",
                "paso3", "En Swagger, usar el botón 'Authorize' e ingresar: Bearer <token> para probar los endpoints protegidos.",
                "paso4", "Los endpoints públicos (sin token) son: GET /, GET /swagger-ui.html, GET /v3/api-docs, GET /actuator/health y los de /api/auth (login, registro, verificar, recuperar).",
                "paso5", "Roles: el admin y el bibliotecario gestionan libros y préstamos de libros; el almacenista gestiona equipos y préstamos de equipos; el aprendiz puede solicitar préstamos y renovaciones.",
                "nota", "El frontend (React) se ejecuta aparte en http://localhost:5173 y consume esta misma API a través del proxy /api."
        ));
        return body;
    }
}