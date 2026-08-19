package co.sena.adso.biblioteca.config;

import com.lambdaworks.crypto.SCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class CompatiblePasswordEncoder implements PasswordEncoder {

    private static final int WERKZEUG_SCRYPT_DKLEN = 64;

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        return bcrypt.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$")) {
            return bcrypt.matches(rawPassword, encodedPassword);
        }
        if (encodedPassword.startsWith("scrypt:")) {
            return matchesWerkzeugScrypt(rawPassword, encodedPassword);
        }
        return false;
    }

    private boolean matchesWerkzeugScrypt(CharSequence rawPassword, String encodedPassword) {
        try {
            // Formato de werkzeug: scrypt:N:r:p$salt_ascii$hash_hex (hashlib.scrypt, dklen=64)
            String[] parts = encodedPassword.split("\\$", 3);
            if (parts.length != 3) {
                return false;
            }
            String[] params = parts[0].split(":");
            if (params.length != 4 || !"scrypt".equals(params[0])) {
                return false;
            }
            int n = Integer.parseInt(params[1]);
            int r = Integer.parseInt(params[2]);
            int p = Integer.parseInt(params[3]);

            byte[] salt = parts[1].getBytes(StandardCharsets.UTF_8);
            byte[] expected = java.util.HexFormat.of().parseHex(parts[2]);

            byte[] actual = SCrypt.scrypt(rawPassword.toString().getBytes(StandardCharsets.UTF_8), salt, n, r, p, WERKZEUG_SCRYPT_DKLEN);
            return MessageDigest.isEqual(actual, expected);
        } catch (Exception e) {
            return false;
        }
    }
}