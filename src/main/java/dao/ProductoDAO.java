package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import conexion.ConexionDB;
import modelo.Producto;

/**
 * DAO para operaciones CRUD sobre la tabla productos.
 */
public class ProductoDAO {

    // Método para insertar un producto
    public int insertar(Producto p) throws SQLException {
        try (Connection conn = ConexionDB.conectar()) {
            ProductColumns columns = productColumns(conn);
            List<String> names = new ArrayList<>(List.of(
                    columns.nombre, columns.descripcion, columns.precio, columns.stock));
            List<Object> values = new ArrayList<>(List.of(
                    p.getNombre(), p.getDescripcion(), p.getPrecio(), p.getStock()));
            if (columns.categoria != null) {
                names.add(columns.categoria);
                values.add(p.getCategoria());
            }
            if (columns.foto != null) {
                names.add(columns.foto);
                values.add(p.getFoto());
            }
            if (columns.fotoTipo != null) {
                names.add(columns.fotoTipo);
                values.add(p.getFotoTipo());
            }

            String placeholders = String.join(", ", java.util.Collections.nCopies(names.size(), "?"));
            String sql = "INSERT INTO productos (" + quotedColumns(names) + ") VALUES (" + placeholders + ")";
            try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                setValues(ps, values);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
            throw new SQLException("No se generó el identificador del producto.");
        }
    }

    // Método para listar productos
    public List<Producto> listar() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        try (Connection conn = ConexionDB.conectar()) {
            ProductColumns columns = productColumns(conn);
            String sql = "SELECT " + selectColumns(columns) + " FROM productos";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(readProduct(rs));
            }
        }
        return lista;
    }

    public Producto obtener(int id) throws SQLException {
        try (Connection conn = ConexionDB.conectar()) {
            ProductColumns columns = productColumns(conn);
            String sql = "SELECT " + selectColumns(columns) + " FROM productos WHERE "
                    + quote(columns.id) + "=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? readProduct(rs) : null;
                }
            }
        }
    }

    // Método para actualizar un producto
    public boolean actualizarProducto(Producto p) throws SQLException {
        try (Connection conn = ConexionDB.conectar()) {
            ProductColumns columns = productColumns(conn);
            List<String> assignments = new ArrayList<>(List.of(
                    columns.nombre, columns.descripcion, columns.precio, columns.stock));
            List<Object> values = new ArrayList<>(List.of(
                    p.getNombre(), p.getDescripcion(), p.getPrecio(), p.getStock()));
            if (columns.categoria != null) {
                assignments.add(columns.categoria);
                values.add(p.getCategoria());
            }
            if (p.getFoto() != null && columns.foto != null) {
                assignments.add(columns.foto);
                values.add(p.getFoto());
            }
            if (p.getFotoTipo() != null && columns.fotoTipo != null) {
                assignments.add(columns.fotoTipo);
                values.add(p.getFotoTipo());
            }

            String sql = "UPDATE productos SET " + assignments.stream()
                    .map(name -> quote(name) + "=?")
                    .collect(java.util.stream.Collectors.joining(", "))
                    + " WHERE " + quote(columns.id) + "=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                setValues(ps, values);
                ps.setInt(values.size() + 1, p.getId());
                return ps.executeUpdate() > 0;
            }
        }
    }

    // Método para eliminar un producto
    public boolean eliminarProducto(int id) throws SQLException {
        try (Connection conn = ConexionDB.conectar()) {
            ProductColumns columns = productColumns(conn);
            String sql = "DELETE FROM productos WHERE " + quote(columns.id) + "=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }
        }
    }

    private ProductColumns productColumns(Connection conn) throws SQLException {
        Map<String, String> available = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM productos WHERE 1=0");
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData metadata = rs.getMetaData();
            for (int i = 1; i <= metadata.getColumnCount(); i++) {
                available.put(metadata.getColumnLabel(i).toLowerCase(Locale.ROOT), metadata.getColumnName(i));
            }
        }
        return new ProductColumns(
                requiredColumn(available, "id_producto", "id"),
                requiredColumn(available, "nombre"),
                requiredColumn(available, "descripción", "descripcion"),
                requiredColumn(available, "precio"),
                requiredColumn(available, "stock"),
                optionalColumn(available, "categoria", "categoría"),
                optionalColumn(available, "foto"),
                optionalColumn(available, "foto_tipo"));
    }

    private String requiredColumn(Map<String, String> available, String... candidates) throws SQLException {
        String column = optionalColumn(available, candidates);
        if (column == null) throw new SQLException("Falta una columna requerida en la tabla productos.");
        return column;
    }

    private String optionalColumn(Map<String, String> available, String... candidates) {
        for (String candidate : candidates) {
            String column = available.get(candidate.toLowerCase(Locale.ROOT));
            if (column != null) return column;
        }
        return null;
    }

    private String selectColumns(ProductColumns columns) {
        return quote(columns.id) + " AS id_producto, "
                + quote(columns.nombre) + " AS nombre, "
                + quote(columns.descripcion) + " AS descripcion, "
                + quote(columns.precio) + " AS precio, "
                + quote(columns.stock) + " AS stock, "
                + selectedColumn(columns.categoria, "categoria") + ", "
                + selectedColumn(columns.foto, "foto") + ", "
                + selectedColumn(columns.fotoTipo, "foto_tipo");
    }

    private String selectedColumn(String column, String alias) {
        return (column == null ? "NULL" : quote(column)) + " AS " + quote(alias);
    }

    private Producto readProduct(ResultSet rs) throws SQLException {
        Producto product = new Producto(rs.getInt("id_producto"), rs.getString("nombre"),
                rs.getString("descripcion"), rs.getDouble("precio"), rs.getInt("stock"),
                rs.getString("categoria"));
        product.setFoto(rs.getBytes("foto"));
        product.setFotoTipo(rs.getString("foto_tipo"));
        return product;
    }

    private String quotedColumns(List<String> columns) {
        return columns.stream().map(this::quote).collect(java.util.stream.Collectors.joining(", "));
    }

    private String quote(String identifier) {
        return "`" + identifier.replace("`", "``") + "`";
    }

    private void setValues(PreparedStatement ps, List<Object> values) throws SQLException {
        for (int i = 0; i < values.size(); i++) ps.setObject(i + 1, values.get(i));
    }

    private static final class ProductColumns {
        private final String id;
        private final String nombre;
        private final String descripcion;
        private final String precio;
        private final String stock;
        private final String categoria;
        private final String foto;
        private final String fotoTipo;

        private ProductColumns(String id, String nombre, String descripcion, String precio,
                String stock, String categoria, String foto, String fotoTipo) {
            this.id = id;
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.precio = precio;
            this.stock = stock;
            this.categoria = categoria;
            this.foto = foto;
            this.fotoTipo = fotoTipo;
        }
    }
}
