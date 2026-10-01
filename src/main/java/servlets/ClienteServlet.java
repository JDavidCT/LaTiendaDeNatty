package servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;

import conexion.ConexionDB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

@WebServlet("/clientes/*")
public class ClienteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    // Atiende el registro y el inicio de sesión de clientes.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getPathInfo();
        if (!"/registro".equals(path) && !"/login".equals(path)) {
            JsonUtil.error(response, 404, "Ruta de clientes no encontrada.");
            return;
        }
        try {
            JsonObject body = JsonUtil.read(request);
            if ("/registro".equals(path)) register(body, response);
            else login(request, body, response);
        } catch (IllegalArgumentException e) {
            JsonUtil.error(response, 400, e.getMessage());
        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                JsonUtil.error(response, 400, "Ya existe una cuenta con ese correo.");
            } else {
                JsonUtil.error(response, 500, "No fue posible procesar la cuenta.");
            }
        }
    }

    // Devuelve los puntos del cliente autenticado.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int id = parsePointsId(request.getPathInfo());
            if (!TokenAuth.ownsClient(request, id) && !TokenAuth.valid(request, "adminToken")) {
                JsonUtil.error(response, 401, "Se requiere iniciar sesión.");
                return;
            }
            try (Connection connection = ConexionDB.conectar();
                 PreparedStatement statement = connection.prepareStatement("SELECT puntos FROM usuarios WHERE id_usuario=?")) {
                statement.setInt(1, id);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) JsonUtil.error(response, 404, "Cliente no encontrado.");
                    else JsonUtil.send(response, 200, Map.of("idCliente", id, "puntos", result.getInt("puntos")));
                }
            }
        } catch (IllegalArgumentException e) {
            JsonUtil.error(response, 400, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.error(response, 500, "No fue posible consultar los puntos.");
        }
    }

    // Actualiza puntos con permiso de administrador; las compras los acumulan automáticamente.
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!TokenAuth.valid(request, "adminToken")) {
            JsonUtil.error(response, 401, "Se requiere una sesión de administrador.");
            return;
        }
        try {
            int id = parsePointsId(request.getPathInfo());
            JsonObject body = JsonUtil.read(request);
            if (!isInteger(body, "puntos")) throw new IllegalArgumentException("El campo puntos debe ser un entero.");
            int points = body.get("puntos").getAsInt();
            if (points < 0) throw new IllegalArgumentException("Los puntos no pueden ser negativos.");
            try (Connection connection = ConexionDB.conectar()) {
                connection.setAutoCommit(false);
                try {
                    int previous;
                    try (PreparedStatement query = connection.prepareStatement("SELECT puntos FROM usuarios WHERE id_usuario=? FOR UPDATE")) {
                        query.setInt(1, id);
                        try (ResultSet result = query.executeQuery()) {
                            if (!result.next()) {
                                connection.rollback();
                                JsonUtil.error(response, 404, "Cliente no encontrado.");
                                return;
                            }
                            previous = result.getInt("puntos");
                        }
                    }
                    try (PreparedStatement update = connection.prepareStatement("UPDATE usuarios SET puntos=? WHERE id_usuario=?")) {
                        update.setInt(1, points);
                        update.setInt(2, id);
                        update.executeUpdate();
                    }
                    int difference = points - previous;
                    if (difference != 0) {
                        try (PreparedStatement log = connection.prepareStatement("INSERT INTO puntos_lealtad(id_usuario, puntos, tipo) VALUES (?, ?, ?)")) {
                            log.setInt(1, id);
                            log.setInt(2, Math.abs(difference));
                            log.setString(3, difference > 0 ? "acumulado" : "redimido");
                            log.executeUpdate();
                        }
                    }
                    connection.commit();
                    JsonUtil.send(response, 200, Map.of("idCliente", id, "puntos", points));
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                }
            }
        } catch (IllegalArgumentException e) {
            JsonUtil.error(response, 400, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.error(response, 500, "No fue posible actualizar los puntos.");
        }
    }

    private void register(JsonObject body, HttpServletResponse response) throws SQLException, IOException {
        String name = JsonUtil.string(body, "nombre");
        String email = JsonUtil.string(body, "correo");
        String password = JsonUtil.string(body, "contrasena");
        if (name == null || name.isBlank() || name.length() > 100 || email == null || email.length() > 100
            || !EMAIL.matcher(email).matches() || password == null || password.length() < 8
            || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Ingresa nombre, correo válido y contraseña de al menos 8 caracteres.");
        }
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt(12));
        String sql = "INSERT INTO usuarios(nombre, correo, password, puntos) VALUES (?, ?, ?, 0)";
        try (Connection connection = ConexionDB.conectar();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name.trim());
            statement.setString(2, email.trim().toLowerCase());
            statement.setString(3, hashed);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                keys.next();
                JsonUtil.send(response, 201, Map.of("id", keys.getInt(1), "nombre", name.trim(), "correo", email.trim().toLowerCase(), "puntos", 0));
            }
        }
    }

    private void login(HttpServletRequest request, JsonObject body, HttpServletResponse response)
            throws SQLException, IOException {
        String email = JsonUtil.string(body, "correo");
        String password = JsonUtil.string(body, "contrasena");
        if (email == null || password == null) throw new IllegalArgumentException("Correo y contraseña son obligatorios.");
        String sql = "SELECT id_usuario, nombre, correo, password, puntos FROM usuarios WHERE correo=?";
        try (Connection connection = ConexionDB.conectar(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email.trim().toLowerCase());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || !passwordMatches(password, result.getString("password"))) {
                    JsonUtil.error(response, 401, "Correo o contraseña incorrectos.");
                    return;
                }
                int id = result.getInt("id_usuario");
                String storedPassword = result.getString("password");
                if (!isBcrypt(storedPassword)) {
                    try (PreparedStatement update = connection.prepareStatement("UPDATE usuarios SET password=? WHERE id_usuario=?")) {
                        update.setString(1, BCrypt.hashpw(password, BCrypt.gensalt(12)));
                        update.setInt(2, id);
                        update.executeUpdate();
                    }
                }
                String token = TokenAuth.issue(request, "clienteToken", id);
                Map<String, Object> customer = new LinkedHashMap<>();
                customer.put("id", id);
                customer.put("nombre", result.getString("nombre"));
                customer.put("correo", result.getString("correo"));
                customer.put("puntos", result.getInt("puntos"));
                customer.put("token", token);
                JsonUtil.send(response, 200, Map.of("token", token, "cliente", customer));
            }
        }
    }

    private boolean passwordMatches(String raw, String stored) {
        if (stored == null) return false;
        if (!isBcrypt(stored)) return stored.equals(raw);
        try { return BCrypt.checkpw(raw, stored); }
        catch (IllegalArgumentException e) { return false; }
    }

    private boolean isBcrypt(String password) {
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }

    private int parsePointsId(String path) {
        if (path == null || !path.matches("/\\d+/puntos")) throw new IllegalArgumentException("Ruta de puntos no válida.");
        return Integer.parseInt(path.substring(1, path.indexOf('/', 1)));
    }

    private boolean isInteger(JsonObject body, String field) {
        if (!body.has(field) || !body.get(field).isJsonPrimitive() || !body.getAsJsonPrimitive(field).isNumber()) return false;
        try { body.get(field).getAsBigDecimal().intValueExact(); return true; }
        catch (ArithmeticException e) { return false; }
    }
}