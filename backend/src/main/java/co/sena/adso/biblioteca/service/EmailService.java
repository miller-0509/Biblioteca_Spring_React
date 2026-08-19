package co.sena.adso.biblioteca.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Servicio de correo electrónico para notificaciones, verificación de cuentas
 * y restablecimiento de credenciales. Envía correos reales mediante SMTP (JavaMailSender)
 * con plantillas HTML responsivas. Si el envío falla, registra el error en consola.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${server.port:31026}")
    private String serverPort;

    @Value("${app.mail.from:caperamiller5@gmail.com}")
    private String mailFrom;

    @Value("${app.mail.sender-name:Biblioteca SENA ADSO}")
    private String senderName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        log.info("📧 EmailService inicializado. JavaMailSender inyectado: {}", mailSender != null);
    }

    public void enviarCorreoVerificacion(String nombres, String correo, String token) {
        String enlaceFrontend = sanitizarUrl(frontendUrl) + "/verificar-email?token=" + token;
        String enlaceBackend = "http://localhost:" + serverPort + "/api/auth/verificar/" + token;
        String asunto = "🔐 Verifica tu correo electrónico - Sistema SENA Biblioteca";

        String htmlContent = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Verificación de Cuenta</title>
              <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #0f172a; color: #334155; margin: 0; padding: 20px; }
                .container { max-width: 580px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.1); }
                .header { background: linear-gradient(135deg, #059669 0%%, #047857 100%%); padding: 32px 24px; text-align: center; color: #ffffff; }
                .header h1 { margin: 0; font-size: 24px; font-weight: 700; letter-spacing: -0.5px; }
                .header p { margin: 6px 0 0; font-size: 14px; opacity: 0.9; }
                .body { padding: 32px 28px; line-height: 1.6; color: #334155; }
                .greeting { font-size: 18px; font-weight: 600; color: #0f172a; margin-bottom: 16px; }
                .btn-container { text-align: center; margin: 32px 0; }
                .btn { display: inline-block; background: #059669; color: #ffffff !important; text-decoration: none; padding: 14px 32px; font-weight: 600; font-size: 15px; border-radius: 8px; box-shadow: 0 4px 12px rgba(5, 150, 105, 0.35); }
                .token-box { background: #f1f5f9; border-left: 4px solid #059669; padding: 12px 16px; margin: 20px 0; border-radius: 4px; font-size: 13px; color: #475569; word-break: break-all; }
                .footer { background: #f8fafc; border-top: 1px solid #e2e8f0; padding: 20px 28px; font-size: 12px; color: #64748b; text-align: center; }
                .warning { font-size: 13px; color: #e11d48; margin-top: 20px; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1>Biblioteca &amp; Centro de Recursos</h1>
                  <p>SENA ADSO · Confirmación de Seguridad</p>
                </div>
                <div class="body">
                  <div class="greeting">¡Hola, %s! 👋</div>
                  <p>Te damos la bienvenida al <strong>Sistema de Gestión de Biblioteca y Almacén SENA</strong>. Para proteger tu cuenta y comenzar a solicitar préstamos de libros y equipos, es obligatorio verificar tu dirección de correo electrónico.</p>
                  
                  <div class="btn-container">
                    <a href="%s" class="btn" target="_blank">✓ Confirmar y Activar mi Cuenta</a>
                  </div>

                  <p>O si prefieres, copia y pega el siguiente enlace directamente en tu navegador:</p>
                  <div class="token-box">%s</div>

                  <p class="warning">⏱ <em>Este enlace de verificación es de un solo uso y expirará en 60 minutos.</em></p>
                  <p style="font-size: 13px; color: #64748b;">Si no realizaste este registro, por favor ignora este mensaje. Ningún préstamo podrá realizarse sin tu confirmación.</p>
                </div>
                <div class="footer">
                  <p>© Servicio Nacional de Aprendizaje - SENA · Sistema Integrado ADSO</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(nombres, enlaceFrontend, enlaceFrontend);

        enviarCorreo(correo, asunto, htmlContent, enlaceFrontend, enlaceBackend);
    }

    public void enviarCorreoRecuperacion(String nombres, String correo, String token) {
        String enlaceFrontend = sanitizarUrl(frontendUrl) + "/restablecer-password/" + token;
        String enlaceBackend = "http://localhost:" + serverPort + "/api/auth/restablecer-password/" + token;
        String asunto = "🔑 Restablecimiento de Contraseña - Sistema SENA Biblioteca";

        String htmlContent = """
            <!DOCTYPE html>
            <html lang="es">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Recuperar Contraseña</title>
              <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #0f172a; color: #334155; margin: 0; padding: 20px; }
                .container { max-width: 580px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.1); }
                .header { background: linear-gradient(135deg, #4f46e5 0%%, #3730a3 100%%); padding: 32px 24px; text-align: center; color: #ffffff; }
                .header h1 { margin: 0; font-size: 24px; font-weight: 700; }
                .body { padding: 32px 28px; line-height: 1.6; color: #334155; }
                .btn-container { text-align: center; margin: 32px 0; }
                .btn { display: inline-block; background: #4f46e5; color: #ffffff !important; text-decoration: none; padding: 14px 32px; font-weight: 600; font-size: 15px; border-radius: 8px; }
                .token-box { background: #f1f5f9; border-left: 4px solid #4f46e5; padding: 12px 16px; margin: 20px 0; font-size: 13px; color: #475569; word-break: break-all; }
                .footer { background: #f8fafc; border-top: 1px solid #e2e8f0; padding: 20px 28px; font-size: 12px; color: #64748b; text-align: center; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1>Recuperación de Contraseña</h1>
                </div>
                <div class="body">
                  <p>Hola <strong>%s</strong>,</p>
                  <p>Hemos recibido una solicitud para restablecer la contraseña de tu cuenta en el Sistema SENA.</p>
                  <div class="btn-container">
                    <a href="%s" class="btn" target="_blank">Restablecer mi Contraseña</a>
                  </div>
                  <p>Enlace directo:</p>
                  <div class="token-box">%s</div>
                  <p style="font-size: 13px; color: #e11d48;">⏱ Este enlace es válido por 60 minutos y solo puede usarse una vez.</p>
                </div>
                <div class="footer">
                  <p>© Servicio Nacional de Aprendizaje - SENA</p>
                </div>
              </div>
            </body>
            </html>
            """.formatted(nombres, enlaceFrontend, enlaceFrontend);

        enviarCorreo(correo, asunto, htmlContent, enlaceFrontend, enlaceBackend);
    }

    public void notificarPrestamo(String nombres, String correo, String tipoNotificacion, String recurso, String detalle) {
        String asunto = "[" + tipoNotificacion + "] - " + recurso + " (SENA)";
        String htmlContent = """
            <div style="font-family: sans-serif; padding: 20px; color: #334155;">
              <h2 style="color: #059669;">Notificación de Préstamo - SENA</h2>
              <p>Hola <strong>%s</strong>,</p>
              <p>%s</p>
              <p><strong>Recurso:</strong> %s</p>
              <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;" />
              <small style="color: #64748b;">Biblioteca &amp; Almacén SENA ADSO</small>
            </div>
            """.formatted(nombres, detalle, recurso);

        enviarCorreo(correo, asunto, htmlContent, null, null);
    }

    public void notificarMulta(String nombres, String correo, String tipoNotificacion, String detalle) {
        String asunto = "⚠️ [" + tipoNotificacion + "] Suspensión - Sistema SENA";
        String htmlContent = """
            <div style="font-family: sans-serif; padding: 20px; color: #334155;">
              <h2 style="color: #e11d48;">Aviso de Suspensión - SENA</h2>
              <p>Hola <strong>%s</strong>,</p>
              <p>%s</p>
              <p style="color: #e11d48;"><strong>Recuerda devolver tus recursos a tiempo para evitar sanciones acumulativas.</strong></p>
              <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;" />
              <small style="color: #64748b;">Biblioteca &amp; Almacén SENA ADSO</small>
            </div>
            """.formatted(nombres, detalle);

        enviarCorreo(correo, asunto, htmlContent, null, null);
    }

    /**
     * Envía el correo de forma síncrona para que los errores sean visibles inmediatamente.
     */
    private void enviarCorreo(String destinatario, String asunto, String htmlBody, String enlaceFrontend, String enlaceBackend) {
        try {
            log.info("📧 Intentando enviar correo a: {} | Asunto: {}", destinatario, asunto);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom, senderName);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("✅ Correo enviado exitosamente vía SMTP a: {}", destinatario);

        } catch (Exception e) {
            log.error("❌ ERROR al enviar correo a {}: {}", destinatario, e.getMessage(), e);
            // Registrar enlaces en consola como respaldo
            log.info("\n" +
                "===================================================================\n" +
                "  📧 CORREO NO ENVIADO [FALLBACK CONSOLA]\n" +
                "  Para:    " + destinatario + "\n" +
                "  Asunto:  " + asunto + "\n" +
                (enlaceFrontend != null ? "  Frontend Link: " + enlaceFrontend + "\n" : "") +
                (enlaceBackend != null ? "  Backend Link:  " + enlaceBackend + "\n" : "") +
                "===================================================================\n");
        }
    }

    private String sanitizarUrl(String url) {
        if (url == null || url.isBlank()) return "http://localhost:5173";
        return url.replaceAll("/+$", "");
    }
}
