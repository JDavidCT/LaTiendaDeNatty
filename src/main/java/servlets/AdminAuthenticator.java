package servlets;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.mindrot.jbcrypt.BCrypt;

public class AdminAuthenticator {
    private static final String USER_ENV = "LATIENDA_ADMIN_USER";
    private static final String PASSWORD_HASH_ENV = "LATIENDA_ADMIN_PASSWORD_HASH";

    public boolean autenticar(String usuario, String contrasena) {
        String configuredUser = System.getenv(USER_ENV);
        String configuredPasswordHash = System.getenv(PASSWORD_HASH_ENV);
        if (configuredUser == null || configuredUser.isBlank()
                || configuredPasswordHash == null || configuredPasswordHash.isBlank()) {
            throw new IllegalStateException("Faltan LATIENDA_ADMIN_USER y LATIENDA_ADMIN_PASSWORD_HASH.");
        }
        if (usuario == null || contrasena == null || contrasena.isEmpty()) return false;

        boolean userMatches = MessageDigest.isEqual(
                configuredUser.getBytes(StandardCharsets.UTF_8),
                usuario.trim().getBytes(StandardCharsets.UTF_8));
        if (!userMatches) return false;

        try {
            return BCrypt.checkpw(contrasena, configuredPasswordHash);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("LATIENDA_ADMIN_PASSWORD_HASH no es un hash BCrypt válido.", e);
        }
    }
}