package servlets;

import java.io.IOException;
import java.util.Map;

import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/api/admin/login", "/api/admin/logout"})
public class AdminAuthServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final AdminAuthenticator adminAuthenticator = new AdminAuthenticator();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean jsonRequest = request.getContentType() != null
                && request.getContentType().toLowerCase().startsWith("application/json");

        if (request.getServletPath().endsWith("/logout")) {
            cerrarSesion(request, response, jsonRequest);
            return;
        }

        try {
            String user;
            String password;
            if (jsonRequest) {
                JsonObject body = JsonUtil.read(request);
                user = JsonUtil.string(body, "usuario");
                password = JsonUtil.string(body, "contrasena");
            } else {
                request.setCharacterEncoding("UTF-8");
                user = request.getParameter("usuario");
                password = request.getParameter("contrasena");
            }

            if (user == null || password == null || user.isBlank() || password.isEmpty()) {
                respuestaLoginFallido(request, response, jsonRequest, 400,
                        "Usuario y contraseña son obligatorios.");
                return;
            }

            if (!adminAuthenticator.autenticar(user, password)) {
                respuestaLoginFallido(request, response, jsonRequest, 401,
                        "Usuario o contraseña incorrectos.");
                return;
            }

            String token = TokenAuth.issue(request, "adminToken", 0);
            if (jsonRequest) {
                JsonUtil.send(response, 200, Map.of("token", token));
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/index.html");
            }
        } catch (IllegalArgumentException e) {
            respuestaLoginFallido(request, response, jsonRequest, 400,
                    "Solicitud de inicio de sesión no válida.");
        } catch (IllegalStateException e) {
            getServletContext().log("La configuración de autenticación del administrador no es válida.", e);
            respuestaLoginFallido(request, response, jsonRequest, 500,
                "El acceso de administrador no está configurado correctamente.");
        }
    }

    private void respuestaLoginFallido(HttpServletRequest request, HttpServletResponse response,
            boolean jsonRequest, int status, String mensaje) throws IOException {
        if (jsonRequest) {
            JsonUtil.error(response, status, mensaje);
        } else {
            String codigoError = status >= 500 ? "config" : "1";
            response.sendRedirect(request.getContextPath() + "/admin/login.html?error=" + codigoError);
        }
    }

    private void cerrarSesion(HttpServletRequest request, HttpServletResponse response,
            boolean jsonRequest) throws IOException {
        var session = request.getSession(false);
        if (session != null) session.invalidate();

        if (jsonRequest || "application/json".equalsIgnoreCase(request.getHeader("Accept"))) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            response.setHeader("Cache-Control", "no-store");
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/login.html");
        }
    }
}