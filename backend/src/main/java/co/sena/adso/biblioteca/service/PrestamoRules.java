package co.sena.adso.biblioteca.service;

import co.sena.adso.biblioteca.entity.EstadoMulta;
import co.sena.adso.biblioteca.entity.EstadoPrestamo;
import co.sena.adso.biblioteca.entity.EstadoPrestamoLibro;
import co.sena.adso.biblioteca.entity.Multa;
import co.sena.adso.biblioteca.entity.RolUsuario;
import co.sena.adso.biblioteca.entity.Usuario;
import co.sena.adso.biblioteca.repository.MultaRepository;
import co.sena.adso.biblioteca.repository.PrestamoLibroRepository;
import co.sena.adso.biblioteca.repository.PrestamoRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Reglas de negocio compartidas entre préstamos de libros y equipos.
 */
@Component
public class PrestamoRules {

    private final PrestamoRepository prestamoRepository;
    private final PrestamoLibroRepository prestamoLibroRepository;
    private final MultaRepository multaRepository;

    public PrestamoRules(PrestamoRepository prestamoRepository,
                         PrestamoLibroRepository prestamoLibroRepository,
                         MultaRepository multaRepository) {
        this.prestamoRepository = prestamoRepository;
        this.prestamoLibroRepository = prestamoLibroRepository;
        this.multaRepository = multaRepository;
    }

    public int limitePrestamos(RolUsuario rol) {
        return switch (rol) {
            case aprendiz -> 3;
            case instructor -> 8;
            case bibliotecario -> 5;
            case almacenista -> 5;
            case administrador -> 999;
        };
    }

    /**
     * Cuenta préstamos activos combinando libros y equipos.
     */
    public long prestamosActivosCount(Usuario usuario) {
        long equipos = prestamoRepository.countByUsuarioIdAndEstadoIn(
            usuario.getId(), List.of(EstadoPrestamo.pendiente, EstadoPrestamo.aceptado));
        long libros = prestamoLibroRepository.countByUsuarioIdAndEstadoIn(
            usuario.getId(), List.of(EstadoPrestamoLibro.pendiente, EstadoPrestamoLibro.aceptado));
        return equipos + libros;
    }

    /**
     * True si el usuario tiene una multa acumulando o una suspensión activa vigente.
     */
    public boolean tieneMultasPendientes(Usuario usuario) {
        return multasPendientesCount(usuario) > 0;
    }

    public long multasPendientesCount(Usuario usuario) {
        LocalDateTime ahora = LocalDateTime.now();
        long acumulando = multaRepository.countByUsuarioIdAndEstadoIn(
            usuario.getId(), List.of(EstadoMulta.acumulando));
        long activas = multaRepository.findByUsuarioId(usuario.getId()).stream()
            .filter(m -> m.getEstado() == EstadoMulta.activa
                && m.getFechaFinSuspension() != null
                && m.getFechaFinSuspension().isAfter(ahora))
            .count();
        return acumulando + activas;
    }

    public boolean limiteRenovacionesAlcanzado(Usuario usuario, int renovacionesAplicadas) {
        int limite = switch (usuario.getRol()) {
            case aprendiz -> 1;
            case instructor -> 2;
            default -> Integer.MAX_VALUE;
        };
        return renovacionesAplicadas >= limite;
    }
}
