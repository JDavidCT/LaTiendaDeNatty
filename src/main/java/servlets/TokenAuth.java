package servlets;

import java.security.SecureRandom;
import java.util.Base64;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

final class TokenAuth {
    private static final SecureRandom RANDOM = new SecureRandom();

    private TokenAuth() {}

    static String issue(HttpServletRequest request, String key, int clientId) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.removeAttribute("adminToken");
        session.removeAttribute("clienteToken");
        session.removeAttribute("clienteId");
        session.setAttribute(key, token);
        if (clientId > 0) session.setAttribute("clienteId", clientId);
        return token;
    }

    static boolean valid(HttpServletRequest request, String key) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        String expected = (String) session.getAttribute(key);
        String header = request.getHeader("Authorization");
        if (expected == null) return false;
        if (header == null) return true;
        return header.startsWith("Bearer ") && expected.equals(header.substring(7));
    }

    static boolean ownsClient(HttpServletRequest request, int clientId) {
        HttpSession session = request.getSession(false);
        return valid(request, "clienteToken") && session != null
                && Integer.valueOf(clientId).equals(session.getAttribute("clienteId"));
    }
}