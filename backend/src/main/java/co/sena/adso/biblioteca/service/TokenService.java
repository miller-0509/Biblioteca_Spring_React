package co.sena.adso.biblioteca.service;

import co.sena.adso.biblioteca.entity.TipoToken;
import co.sena.adso.biblioteca.entity.TokenVerificacion;
import co.sena.adso.biblioteca.exception.BusinessException;
import co.sena.adso.biblioteca.repository.TokenVerificacionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class TokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final TokenVerificacionRepository tokenRepository;

    @Value("${tokens.expiracion-minutos:60}")
    private long expiracionMinutos;

    public TokenService(TokenVerificacionRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public String generarToken(String correo, TipoToken tipo) {
        tokenRepository.findTopByCorreoAndTipoOrderByCreadoEnDesc(correo, tipo)
            .ifPresent(old -> {
                old.setUsado(true);
                tokenRepository.save(old);
            });
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);
        TokenVerificacion entidad = new TokenVerificacion(token, correo.toLowerCase(), tipo,
            LocalDateTime.now().plusMinutes(expiracionMinutos));
        tokenRepository.save(entidad);
        return token;
    }

    /**
     * Valida y consume (one-time) un token. Retorna el correo asociado.
     */
    @Transactional
    public String consumirToken(String token, TipoToken tipo) {
        TokenVerificacion entidad = tokenRepository.findByToken(token)
            .orElseThrow(() -> new BusinessException("El enlace es inválido."));
        if (entidad.getTipo() != tipo) {
            throw new BusinessException("El enlace es inválido.");
        }
        if (entidad.getUsado()) {
            throw new BusinessException("Este enlace ya fue utilizado.");
        }
        if (entidad.getExpiracion().isBefore(LocalDateTime.now())) {
            throw new BusinessException("El enlace ha expirado. Solicita uno nuevo.");
        }
        entidad.setUsado(true);
        tokenRepository.save(entidad);
        return entidad.getCorreo();
    }
}
