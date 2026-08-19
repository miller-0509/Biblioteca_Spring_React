package co.sena.adso.biblioteca.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Escucha el evento de inicio completo de la aplicación (ApplicationReadyEvent)
 * e imprime una guía visual para orientar al aprendiz sobre cómo y dónde probar la API.
 */
@Component
public class StartupListener implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${server.port:8080}")
    private String port;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        System.out.println("\n====================================================================");
        System.out.println("🚀 ¡La API de Biblioteca está lista y funcionando con éxito!");
        System.out.println("====================================================================");
        System.out.println("📍 Perfil activo: " + activeProfile);
        System.out.println("📍 Puerto del servidor: " + port);
        System.out.println("📍 URL base local: http://localhost:" + port);
        System.out.println("📍 Documentación interactiva (Swagger UI):");
        System.out.println("   http://localhost:" + port + "/swagger-ui/index.html");
        System.out.println("📍 Endpoint principal (listar libros):");
        System.out.println("   http://localhost:" + port + "/api/libros");
        System.out.println("📍 Consola H2 (Base de datos en memoria si está activa):");
        System.out.println("   http://localhost:" + port + "/h2-console");
        System.out.println("====================================================================\n");
    }
}
