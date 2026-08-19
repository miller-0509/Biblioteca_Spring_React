package co.sena.adso.biblioteca.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Servicio de correo. En desarrollo no se envía correo real: los enlaces y
 * tokens se imprimen en consola para poder probar el flujo de verificación y
 * recuperación. Si en producción se configura SMTP (spring.mail.*), basta con
 * sustituir los log.info por un envío con JavaMailSender.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${server.port:8080}")
    private String port;

    public void enviarCorreoVerificacion(String nombres, String correo, String token) {
        String enlace = "http://localhost:" + port + "/api/auth/verificar/" + token;
        log.info("\n" +
            "===================================================================\n" +
            "  📧 EMAIL (dev) -> " + correo + "\n" +
            "  Asunto: Verifica tu correo - Sistema SENA Biblioteca\n" +
            "  Hola " + nombres + "! Confirma tu cuenta:\n" +
            "  " + enlace + "\n" +
            "  (Este enlace expira en 60 minutos)\n" +
            "===================================================================\n");
    }

    public void enviarCorreoRecuperacion(String nombres, String correo, String token) {
        String enlace = "http://localhost:" + port + "/api/auth/restablecer-password/" + token;
        log.info("\n" +
            "===================================================================\n" +
            "  📧 EMAIL (dev) -> " + correo + "\n" +
            "  Asunto: Recupera tu contraseña - Sistema SENA Biblioteca\n" +
            "  Hola " + nombres + "! Restablece tu contraseña:\n" +
            "  " + enlace + "\n" +
            "  (Este enlace expira en 60 minutos y solo puede usarse una vez)\n" +
            "===================================================================\n");
    }

    public void notificarPrestamo(String nombres, String correo, String tipoNotificacion, String recurso, String detalle) {
        log.info("\n" +
            "===================================================================\n" +
            "  📧 EMAIL (dev) -> " + correo + "\n" +
            "  Asunto: [" + tipoNotificacion + "] - " + recurso + "\n" +
            "  Hola " + nombres + "!\n" +
            "  " + detalle + "\n" +
            "===================================================================\n");
    }

    public void notificarMulta(String nombres, String correo, String tipoNotificacion, String detalle) {
        log.info("\n" +
            "===================================================================\n" +
            "  📧 EMAIL (dev) -> " + correo + "\n" +
            "  Asunto: [" + tipoNotificacion + "] Suspensión - Sistema SENA\n" +
            "  Hola " + nombres + "!\n" +
            "  " + detalle + "\n" +
            "===================================================================\n");
    }
}
