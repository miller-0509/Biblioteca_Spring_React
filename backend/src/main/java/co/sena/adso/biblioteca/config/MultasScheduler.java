package co.sena.adso.biblioteca.config;

import co.sena.adso.biblioteca.service.MultaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Cron diario de multas: crea/actualiza multas por retraso y cierra suspensiones cumplidas.
 * Equivalente a cron_multas.py de la referencia.
 */
@Component
public class MultasScheduler {

    private static final Logger log = LoggerFactory.getLogger(MultasScheduler.class);

    private final MultaService multaService;

    public MultasScheduler(MultaService multaService) {
        this.multaService = multaService;
    }

    @Scheduled(cron = "${multas.cron:0 0 2 * * *}")
    public void ejecutarActualizacionDiaria() {
        try {
            multaService.actualizarMultasDiarias();
        } catch (Exception e) {
            log.error("Fallo en cron de multas: {}", e.getMessage(), e);
        }
    }
}
