package servlets;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import conexion.ConexionDB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/compras/*")
public class CompraServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Registra una compra, descuenta existencias y acumula puntos en una sola transacción.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer clientId = authenticatedClient(request);
        if (clientId == null) {
            JsonUtil.error(response, 401, "Inicia sesión para confirmar la compra.");
            return;
        }
        try {
            JsonObject body = JsonUtil.read(request);
            if (!body.has("productos") || !body.get("productos").isJsonArray()) {
                throw new IllegalArgumentException("El campo productos debe ser una lista.");
            }
            JsonArray items = body.getAsJsonArray("productos");
            if (items.isEmpty()) throw new IllegalArgumentException("La compra debe incluir productos.");
            BigDecimal total = BigDecimal.ZERO;
            int gainedPoints;
            int pointsBalance;
            int purchaseId;
            try (Connection connection = ConexionDB.conectar()) {
                connection.setAutoCommit(false);
                boolean committed = false;
                try {
                    try (PreparedStatement order = connection.prepareStatement(
                            "INSERT INTO pedidos(id_usuario, total, estado) VALUES (?, 0, 'pendiente')", Statement.RETURN_GENERATED_KEYS)) {
                        order.setInt(1, clientId);
                        order.executeUpdate();
                        try (ResultSet keys = order.getGeneratedKeys()) {
                            keys.next();
                            purchaseId = keys.getInt(1);
                        }
                    }
                    for (var itemElement : items) {
                        if (!itemElement.isJsonObject()) throw new IllegalArgumentException("Cada producto debe ser un objeto.");
                        JsonObject item = itemElement.getAsJsonObject();
                        if (!isInteger(item, "id_producto") || !isInteger(item, "cantidad")) {
                            throw new IllegalArgumentException("Cada producto requiere id_producto y cantidad enteros.");
                        }
                        int productId = item.get("id_producto").getAsInt();
                        int quantity = item.get("cantidad").getAsInt();
                        if (productId < 1 || quantity < 1) throw new IllegalArgumentException("Producto o cantidad no válidos.");
                        String name;
                        BigDecimal price;
                        int stock;
                        try (PreparedStatement query = connection.prepareStatement(
                                "SELECT nombre, precio, stock FROM productos WHERE id_producto=? FOR UPDATE")) {
                            query.setInt(1, productId);
                            try (ResultSet result = query.executeQuery()) {
                                if (!result.next()) throw new IllegalArgumentException("Uno de los productos ya no existe.");
                                name = result.getString("nombre");
                                price = result.getBigDecimal("precio");
                                stock = result.getInt("stock");
                            }
                        }
                        if (stock < quantity) throw new IllegalArgumentException("Stock insuficiente para: " + name + ".");
                        try (PreparedStatement update = connection.prepareStatement("UPDATE productos SET stock=stock-? WHERE id_producto=?")) {
                            update.setInt(1, quantity);
                            update.setInt(2, productId);
                            update.executeUpdate();
                        }
                        try (PreparedStatement detail = connection.prepareStatement(
                                "INSERT INTO detalle_pedido(id_pedidos, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)")) {
                            detail.setInt(1, purchaseId);
                            detail.setInt(2, productId);
                            detail.setInt(3, quantity);
                            detail.setBigDecimal(4, price);
                            detail.executeUpdate();
                        }
                        total = total.add(price.multiply(BigDecimal.valueOf(quantity)));
                        if (total.compareTo(new BigDecimal("99999999.99")) > 0) {
                            throw new IllegalArgumentException("El total de la compra supera el límite permitido.");
                        }
                    }
                    try (PreparedStatement updateOrder = connection.prepareStatement("UPDATE pedidos SET total=? WHERE id_pedidos=?")) {
                        updateOrder.setBigDecimal(1, total);
                        updateOrder.setInt(2, purchaseId);
                        updateOrder.executeUpdate();
                    }
                    gainedPoints = total.divideToIntegralValue(new BigDecimal("10000")).intValueExact();
                    try (PreparedStatement updateClient = connection.prepareStatement("UPDATE usuarios SET puntos=puntos+? WHERE id_usuario=?")) {
                        updateClient.setInt(1, gainedPoints);
                        updateClient.setInt(2, clientId);
                        if (updateClient.executeUpdate() != 1) throw new IllegalArgumentException("Cliente no encontrado.");
                    }
                    if (gainedPoints > 0) {
                        try (PreparedStatement points = connection.prepareStatement(
                                "INSERT INTO puntos_lealtad(id_usuario, puntos, tipo) VALUES (?, ?, 'acumulado')")) {
                            points.setInt(1, clientId);
                            points.setInt(2, gainedPoints);
                            points.executeUpdate();
                        }
                    }
                    try (PreparedStatement queryPoints = connection.prepareStatement("SELECT puntos FROM usuarios WHERE id_usuario=?")) {
                        queryPoints.setInt(1, clientId);
                        try (ResultSet result = queryPoints.executeQuery()) {
                            if (!result.next()) throw new IllegalArgumentException("Cliente no encontrado.");
                            pointsBalance = result.getInt("puntos");
                        }
                    }
                    connection.commit();
                    committed = true;
                } finally {
                    if (!committed) connection.rollback();
                }
            }
                JsonUtil.send(response, 201, Map.of("idCompra", purchaseId, "total", total, "puntosGanados", gainedPoints,
                    "puntos", pointsBalance));
        } catch (IllegalArgumentException e) {
            JsonUtil.error(response, 400, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.error(response, 500, "No fue posible registrar la compra.");
        }
    }

    // Devuelve las compras del cliente autenticado, con el detalle de cada una.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer authenticated = authenticatedClient(request);
        if (authenticated == null) {
            JsonUtil.error(response, 401, "Inicia sesión para consultar las compras.");
            return;
        }
        try {
            int clientId = parseId(request.getPathInfo());
            if (clientId != authenticated) {
                JsonUtil.error(response, 401, "No puedes consultar las compras de otro cliente.");
                return;
            }
            List<Map<String, Object>> orders = new ArrayList<>();
            try (Connection connection = ConexionDB.conectar();
                 PreparedStatement query = connection.prepareStatement(
                         "SELECT id_pedidos, fecha, total, estado FROM pedidos WHERE id_usuario=? ORDER BY fecha DESC")) {
                query.setInt(1, clientId);
                try (ResultSet result = query.executeQuery()) {
                    while (result.next()) {
                        Map<String, Object> order = new LinkedHashMap<>();
                        int id = result.getInt("id_pedidos");
                        order.put("idCompra", id);
                        order.put("fecha", result.getTimestamp("fecha"));
                        order.put("total", result.getBigDecimal("total"));
                        order.put("estado", result.getString("estado"));
                        order.put("productos", loadItems(connection, id));
                        orders.add(order);
                    }
                }
            }
            JsonUtil.send(response, 200, orders);
        } catch (IllegalArgumentException e) {
            JsonUtil.error(response, 400, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.error(response, 500, "No fue posible consultar el historial.");
        }
    }

    private List<Map<String, Object>> loadItems(Connection connection, int purchaseId) throws SQLException {
        List<Map<String, Object>> items = new ArrayList<>();
        String sql = "SELECT d.id_producto, p.nombre, d.cantidad, d.precio_unitario FROM detalle_pedido d "
                + "JOIN productos p ON p.id_producto=d.id_producto WHERE d.id_pedidos=?";
        try (PreparedStatement query = connection.prepareStatement(sql)) {
            query.setInt(1, purchaseId);
            try (ResultSet result = query.executeQuery()) {
                while (result.next()) {
                    items.add(Map.of("id_producto", result.getInt("id_producto"), "nombre", result.getString("nombre"),
                            "cantidad", result.getInt("cantidad"), "precio", result.getBigDecimal("precio_unitario")));
                }
            }
        }
        return items;
    }

    private Integer authenticatedClient(HttpServletRequest request) {
        var session = request.getSession(false);
        if (!TokenAuth.valid(request, "clienteToken") || session == null) return null;
        return (Integer) session.getAttribute("clienteId");
    }

    private int parseId(String path) {
        if (path == null || !path.matches("/\\d+")) throw new IllegalArgumentException("Identificador de cliente no válido.");
        return Integer.parseInt(path.substring(1));
    }

    private boolean isInteger(JsonObject body, String field) {
        if (!body.has(field) || !body.get(field).isJsonPrimitive() || !body.getAsJsonPrimitive(field).isNumber()) return false;
        try { body.get(field).getAsBigDecimal().intValueExact(); return true; }
        catch (ArithmeticException e) { return false; }
    }
}