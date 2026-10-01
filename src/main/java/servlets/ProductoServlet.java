package servlets;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

import dao.ProductoDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modelo.Producto;

/**
 * Servlet para gestionar productos de la tienda.
 * Maneja las operaciones de listado, inserción, actualización y eliminación.
 */
@WebServlet("/productos/*")
public class ProductoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ProductoDAO dao = new ProductoDAO();

    @Override
    // Lista productos o consulta uno por su identificador.
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String path = request.getPathInfo();
            if (path == null || "/".equals(path)) {
                List<Map<String, Object>> products = new ArrayList<>();
                for (Producto product : dao.listar()) products.add(toJson(product));
                JsonUtil.send(response, 200, products);
                return;
            }
            Producto product = dao.obtener(parseId(path));
            if (product == null) JsonUtil.error(response, 404, "Producto no encontrado.");
            else JsonUtil.send(response, 200, toJson(product));
        } catch (IllegalArgumentException e) {
            JsonUtil.error(response, 400, e.getMessage());
        } catch (SQLException e) {
            getServletContext().log("No fue posible consultar los productos.", e);
            JsonUtil.error(response, 500, "No fue posible consultar los productos.");
        }
    }

    @Override
    // Crea un producto y guarda su foto cuando se envía en Base64.
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!TokenAuth.valid(request, "adminToken")) {
            JsonUtil.error(response, 401, "Se requiere una sesión de administrador.");
            return;
        }
        try {
            Producto product = fromJson(JsonUtil.read(request));
            int id = dao.insertar(product);
            product.setId(id);
            JsonUtil.send(response, 201, toJson(product));
        } catch (IllegalArgumentException e) {
            JsonUtil.error(response, 400, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.error(response, 500, "No fue posible guardar el producto.");
        }
    }

    @Override
    // Actualiza un producto existente.
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!TokenAuth.valid(request, "adminToken")) {
            JsonUtil.error(response, 401, "Se requiere una sesión de administrador.");
            return;
        }
        try {
            int id = parseId(request.getPathInfo());
            Producto product = fromJson(JsonUtil.read(request));
            product.setId(id);
            if (!dao.actualizarProducto(product)) JsonUtil.error(response, 404, "Producto no encontrado.");
            else JsonUtil.send(response, 200, toJson(dao.obtener(id)));
        } catch (IllegalArgumentException e) {
            JsonUtil.error(response, 400, e.getMessage());
        } catch (SQLException e) {
            JsonUtil.error(response, 500, "No fue posible actualizar el producto.");
        }
    }

    @Override
    // Elimina un producto existente.
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!TokenAuth.valid(request, "adminToken")) {
            JsonUtil.error(response, 401, "Se requiere una sesión de administrador.");
            return;
        }
        try {
            if (!dao.eliminarProducto(parseId(request.getPathInfo()))) {
                JsonUtil.error(response, 404, "Producto no encontrado.");
            } else {
                JsonUtil.send(response, 200, Map.of("mensaje", "Producto eliminado."));
            }
        } catch (IllegalArgumentException e) {
            JsonUtil.error(response, 400, e.getMessage());
        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                JsonUtil.error(response, 409, "No se puede eliminar un producto asociado a compras.");
            } else {
                JsonUtil.error(response, 500, "No fue posible eliminar el producto.");
            }
        }
    }

    private int parseId(String path) {
        if (path == null || !path.matches("/\\d+")) throw new IllegalArgumentException("Identificador de producto no válido.");
        return Integer.parseInt(path.substring(1));
    }

    private Producto fromJson(JsonObject body) {
        String name = JsonUtil.string(body, "nombre");
        String description = JsonUtil.string(body, "descripcion");
        String category = JsonUtil.string(body, "categoria");
        if (name == null || name.isBlank() || name.length() > 100 || description == null
                || category == null || category.isBlank() || category.length() > 50
                || !isNumber(body, "precio") || !isInteger(body, "stock")) {
            throw new IllegalArgumentException("Nombre, descripción, precio, stock y categoría son obligatorios.");
        }
        double price = body.get("precio").getAsDouble();
        int stock = body.get("stock").getAsInt();
        if (!Double.isFinite(price) || price < 0 || price > 99999999.99 || stock < 0) {
            throw new IllegalArgumentException("Precio y stock deben ser valores positivos.");
        }
        Producto product = new Producto(0, name.trim(), description.trim(), price, stock, category.trim());
        String image = JsonUtil.string(body, "fotoBase64");
        if (image != null && !image.isBlank()) {
            int comma = image.indexOf(',');
            if (comma < 0) throw new IllegalArgumentException("La foto debe ser una imagen codificada en Base64.");
            String header = image.substring(0, comma);
            if (!header.matches("data:image/(jpeg|png|gif|webp);base64")) {
                throw new IllegalArgumentException("Formato de imagen no permitido.");
            }
            String mime = header.substring(5, header.indexOf(';'));
            byte[] bytes = Base64.getDecoder().decode(image.substring(comma + 1));
            if (bytes.length > 5 * 1024 * 1024) throw new IllegalArgumentException("La foto supera el límite de 5 MB.");
            product.setFoto(bytes);
            product.setFotoTipo(mime);
        }
        return product;
    }

    private boolean isNumber(JsonObject body, String field) {
        return body.has(field) && body.get(field).isJsonPrimitive() && body.getAsJsonPrimitive(field).isNumber();
    }

    private boolean isInteger(JsonObject body, String field) {
        if (!isNumber(body, field)) return false;
        try { body.get(field).getAsBigDecimal().intValueExact(); return true; }
        catch (ArithmeticException e) { return false; }
    }

    private Map<String, Object> toJson(Producto product) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", product.getId());
        result.put("id_producto", product.getId());
        result.put("nombre", product.getNombre());
        result.put("descripcion", product.getDescripcion());
        result.put("precio", BigDecimal.valueOf(product.getPrecio()));
        result.put("stock", product.getStock());
        result.put("categoria", product.getCategoria());
        if (product.getFoto() != null) {
            result.put("fotoUrl", "data:" + product.getFotoTipo() + ";base64," + Base64.getEncoder().encodeToString(product.getFoto()));
        }
        return result;
    }
}
