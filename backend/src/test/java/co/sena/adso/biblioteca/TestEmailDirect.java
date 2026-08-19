package co.sena.adso.biblioteca;

import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class TestEmailDirect {

    @Test
    void testEnvioCorreoRealGmail() throws Exception {
        System.out.println(">>> Enviando correo real a juegosmiller58@gmail.com...");
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("smtp.gmail.com");
        sender.setPort(587);
        sender.setUsername("caperamiller5@gmail.com");
        sender.setPassword("mkxfetdtmgeyaypp");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom("caperamiller5@gmail.com", "Biblioteca SENA ADSO");
        helper.setTo("juegosmiller58@gmail.com");
        helper.setSubject("\uD83D\uDD10 Verifica tu correo - Sistema SENA Biblioteca");
        helper.setText("""
            <!DOCTYPE html>
            <html lang="es">
            <head><meta charset="UTF-8"></head>
            <body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #0f172a; margin: 0; padding: 20px;">
              <div style="max-width: 580px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.1);">
                <div style="background: linear-gradient(135deg, #059669 0%, #047857 100%); padding: 32px 24px; text-align: center; color: #ffffff;">
                  <h1 style="margin: 0; font-size: 24px; font-weight: 700;">Biblioteca & Centro de Recursos</h1>
                  <p style="margin: 6px 0 0; font-size: 14px; opacity: 0.9;">SENA ADSO · Prueba de Correo</p>
                </div>
                <div style="padding: 32px 28px; line-height: 1.6; color: #334155;">
                  <div style="font-size: 18px; font-weight: 600; color: #0f172a; margin-bottom: 16px;">¡Hola, Miller! 👋</div>
                  <p>Este es un correo de prueba enviado desde el <strong>Sistema de Gestión de Biblioteca y Almacén SENA</strong>.</p>
                  <p>Si estás leyendo este mensaje, significa que el servicio de correo electrónico está funcionando correctamente. ✅</p>
                  <div style="text-align: center; margin: 32px 0;">
                    <a href="http://localhost:5173" style="display: inline-block; background: #059669; color: #ffffff !important; text-decoration: none; padding: 14px 32px; font-weight: 600; font-size: 15px; border-radius: 8px; box-shadow: 0 4px 12px rgba(5, 150, 105, 0.35);">✓ Ir al Sistema</a>
                  </div>
                  <p style="font-size: 13px; color: #64748b;">Este correo fue enviado automáticamente como prueba de verificación del sistema SMTP.</p>
                </div>
                <div style="background: #f8fafc; border-top: 1px solid #e2e8f0; padding: 20px 28px; font-size: 12px; color: #64748b; text-align: center;">
                  <p>© Servicio Nacional de Aprendizaje - SENA · Sistema Integrado ADSO</p>
                </div>
              </div>
            </body>
            </html>
            """, true);

        sender.send(message);
        System.out.println(">>> [EXITO] Correo enviado a juegosmiller58@gmail.com");
    }
}
