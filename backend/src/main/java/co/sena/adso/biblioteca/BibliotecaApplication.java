package co.sena.adso.biblioteca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "co.sena.adso.biblioteca.entity")
@EnableJpaRepositories(basePackages = "co.sena.adso.biblioteca.repository")
public class BibliotecaApplication {
    public static void main(String[] args) {
        try {
            SpringApplication.run(BibliotecaApplication.class, args);
        } catch (Exception e) {
            Throwable cause = e;
            while (cause != null) {
                String causeName = cause.getClass().getName();
                String causeMsg = cause.getMessage() != null ? cause.getMessage() : "";
                
                if (causeName.contains("ConnectException") || 
                    causeMsg.contains("Connection refused") ||
                    causeName.contains("CommunicationsException") ||
                    causeName.contains("DataAccessResourceFailureException") ||
                    causeName.contains("UnableToAcquireConnectionException")) {
                    
                    System.err.println("\n====================================================================");
                    System.err.println("❌ ERROR FATAL: NO SE PUDO CONECTAR A LA BASE DE DATOS");
                    System.err.println("====================================================================");
                    System.err.println("Por favor verifica que:");
                    System.err.println("1. PostgreSQL esté instalado y ejecutándose.");
                    System.err.println("2. Las credenciales de base de datos sean correctas en application.properties o .env");
                    System.err.println("3. La base de datos exista.");
                    System.err.println("====================================================================\n");
                    System.exit(1);
                }
                cause = cause.getCause();
            }
            throw e;
        }
    }
}
