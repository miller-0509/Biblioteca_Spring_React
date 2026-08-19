package co.sena.adso.biblioteca.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Biblioteca API")
                .version("1.0.0")
                .description("API REST para gestión de préstamos de libros - SENA ADSO 2026")
                .contact(new Contact()
                    .name("SENA ADSO")
                    .email("adso@sena.edu.co"))
                .license(new License()
                    .name("MIT")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server().url("http://localhost:31026").description("Servidor local (desarrollo)"),
                new Server().url("https://api-produccion.com").description("Servidor de producción")
            ));
    }
}
