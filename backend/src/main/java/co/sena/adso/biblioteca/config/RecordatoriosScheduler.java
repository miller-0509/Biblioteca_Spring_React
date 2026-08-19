package co.sena.adso.biblioteca.config;

import co.sena.adso.biblioteca.entity.EstadoPrestamo;
import co.sena.adso.biblioteca.entity.EstadoPrestamoLibro;
import co.sena.adso.biblioteca.entity.Prestamo;
import co.sena.adso.biblioteca.entity.PrestamoLibro;
import co.sena.adso.biblioteca.repository.PrestamoLibroRepository;
import co.sena.adso.biblioteca.repository.PrestamoRepository;
import co.sena.adso.biblioteca.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cron de recordatorios: envía avisos de "próximo a vencer" (menos de 24h) y
 * "vencido" para préstamos activos de libros y equipos.
 * Equivalente a enviar_recordatorios.py de la referencia.
 */
@Component
public class RecordatoriosScheduler {

    private static final Logger log = LoggerFactory.getLogger(RecordatoriosScheduler.class);

    private final PrestamoRepository prestamoRepository;
    private final PrestamoLibroRepository prestamoLibroRepository;
    private final EmailService emailService;

    public RecordatoriosScheduler(PrestamoRepository prestamoRepository,
                                  PrestamoLibroRepository prestamoLibroRepository,
                                  EmailService emailService) {
        this.prestamoRepository = prestamoRepository;
        this.prestamoLibroRepository = prestamoLibroRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "${recordatorios.cron:0 0 8 * * *}")
    public void procesarRecordatorios() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime unDiaDespues = ahora.plusDays(1);
        int enviados = 0;

        for (Prestamo p : prestamoRepository.findByEstadoOrderByFechaSolicitudDesc(EstadoPrestamo.aceptado)) {
            enviados += recordatorioEquipo(p, ahora, unDiaDespues);
        }
        for (PrestamoLibro l : prestamoLibroRepository.findByEstadoOrderByFechaSolicitudDesc(EstadoPrestamoLibro.aceptado)) {
            enviados += recordatorioLibro(l, ahora, unDiaDespues);
        }
        log.info("Recordatorios: notificaciones enviadas={}", enviados);
    }

    private int recordatorioEquipo(Prestamo p, LocalDateTime ahora, LocalDateTime unDiaDespues) {
        if (p.getFechaDevolucionEsperada() == null || p.getUsuario() == null) {
            return 0;
        }
        LocalDateTime limite = p.getFechaDevolucionEsperada();
        if (limite.isBefore(ahora) && !Boolean.TRUE.equals(p.getNotificacionVencidoEnviada())) {
            emailService.notificarPrestamo(p.getUsuario().getNombres(), p.getUsuario().getCorreo(),
                "vencido", p.getEquipo().getNombre(), "Tu préstamo está vencido. Devuelve el recurso para evitar sanciones.");
            p.setNotificacionVencidoEnviada(true);
            p.setNotificacionVencimientoEnviada(true);
            prestamoRepository.save(p);
            return 1;
        }
        if (!limite.isBefore(ahora) && !limite.isAfter(unDiaDespues)
                && !Boolean.TRUE.equals(p.getNotificacionVencimientoEnviada())) {
            emailService.notificarPrestamo(p.getUsuario().getNombres(), p.getUsuario().getCorreo(),
                "proximo_vencer", p.getEquipo().getNombre(), "Tu préstamo vence el " + limite + ". Realiza la devolución a tiempo.");
            p.setNotificacionVencimientoEnviada(true);
            prestamoRepository.save(p);
            return 1;
        }
        return 0;
    }

    private int recordatorioLibro(PrestamoLibro l, LocalDateTime ahora, LocalDateTime unDiaDespues) {
        if (l.getFechaDevolucionEsperada() == null || l.getUsuario() == null) {
            return 0;
        }
        LocalDateTime limite = l.getFechaDevolucionEsperada();
        if (limite.isBefore(ahora) && !Boolean.TRUE.equals(l.getNotificacionVencidoEnviada())) {
            emailService.notificarPrestamo(l.getUsuario().getNombres(), l.getUsuario().getCorreo(),
                "vencido", l.getLibro().getTitulo(), "Tu préstamo está vencido. Devuelve el recurso para evitar sanciones.");
            l.setNotificacionVencidoEnviada(true);
            l.setNotificacionVencimientoEnviada(true);
            prestamoLibroRepository.save(l);
            return 1;
        }
        if (!limite.isBefore(ahora) && !limite.isAfter(unDiaDespues)
                && !Boolean.TRUE.equals(l.getNotificacionVencimientoEnviada())) {
            emailService.notificarPrestamo(l.getUsuario().getNombres(), l.getUsuario().getCorreo(),
                "proximo_vencer", l.getLibro().getTitulo(), "Tu préstamo vence el " + limite + ". Realiza la devolución a tiempo.");
            l.setNotificacionVencimientoEnviada(true);
            prestamoLibroRepository.save(l);
            return 1;
        }
        return 0;
    }
}
