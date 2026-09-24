package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import conexion.ConexionDB;
import modelo.Producto;

/**
 * DAO para operaciones CRUD sobre la tabla productos.
 */
public class ProductoDAO {

    // Método para insertar un producto
    public void insertar(Producto p) throws SQLException {
        String sql = "INSERT INTO productos(nombre, `descripción`, precio, stock, categoria) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setDouble(3, p.getPrecio());
            ps.setInt(4, p.getStock());
            ps.setString(5, p.getCategoria());
            ps.executeUpdate();
            System.out.println("✅ Producto insertado correctamente.");
        } catch (SQLException e) {
            System.out.println("❌ Error al insertar producto: " + e.getMessage());
            throw e;
        }
    }

    // Método para listar productos
    public List<Producto> listar() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM productos";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Producto p = new Producto(
                    rs.getInt("id_producto"),
                    rs.getString("nombre"),
                    rs.getString("descripción"),
                    rs.getDouble("precio"),
                    rs.getInt("stock"),
                    rs.getString("categoria")
                );
                lista.add(p);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al listar productos: " + e.getMessage());
            throw e;
        }
        return lista;
    }

    // Método para actualizar un producto
    public boolean actualizarProducto(Producto p) throws SQLException {
        String sql = "UPDATE productos SET nombre=?, `descripción`=?, precio=?, stock=?, categoria=? WHERE id_producto=?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setDouble(3, p.getPrecio());
            ps.setInt(4, p.getStock());
            ps.setString(5, p.getCategoria());
            ps.setInt(6, p.getId());
            int filas = ps.executeUpdate();
            if (filas > 0) {
                System.out.println("✅ Producto actualizado correctamente.");
            }
            return filas > 0;
        }
    }

    // Método para eliminar un producto
    public boolean eliminarProducto(int id) throws SQLException {
        String sql = "DELETE FROM productos WHERE id_producto=?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            if (filas > 0) {
                System.out.println("✅ Producto eliminado correctamente.");
            }
            return filas > 0;
        }
    }
}
