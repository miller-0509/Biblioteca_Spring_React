package co.sena.adso.biblioteca.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompatiblePasswordEncoderTest {

    private final CompatiblePasswordEncoder encoder = new CompatiblePasswordEncoder();

    @Test
    void bcryptRoundTrip() {
        String hash = encoder.encode("Admin1234");
        assertTrue(hash.startsWith("$2"));
        assertTrue(encoder.matches("Admin1234", hash));
        assertFalse(encoder.matches("otraClave", hash));
    }

    @Test
    void verificaHashScryptDeWerkzeug() {
        String werkzeug = "scrypt:32768:8:1$IIlOdZ7pZAhZ4agS$576de9e1c42a8cb4134b32ea37516d07709c3d41b306625e49f8a151b54c7752dd89933f2ee41d52f3cb7b00e4c6d030b6c8f8fbb8553b90e8ccc0f82dc590ff";
        assertTrue(encoder.matches("PruebaScrypt123", werkzeug));
        assertFalse(encoder.matches("PruebaScrypt124", werkzeug));
    }

    @Test
    void valoresInvalidos() {
        assertFalse(encoder.matches("x", null));
        assertFalse(encoder.matches("x", ""));
        assertFalse(encoder.matches("x", "$2a$10$hashInvalido"));
        assertFalse(encoder.matches("x", "scrypt:malformado"));
    }
}