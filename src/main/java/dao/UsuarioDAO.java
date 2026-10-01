package dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import conexion.ConexionDB;
import modelo.Usuario;


public class UsuarioDAO {
    // Método para insertar un nuevo usuario en la base de datos
    public boolean insertarUsuario(Usuario usuario) {
        boolean resultado = false;
        Connection conn = null;
        PreparedStatement stmt = null;

        try { 
            conn = ConexionDB.conectar();
            String sql = "INSERT INTO usuarios (nombre, correo, password, puntos) VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getCorreo());
            stmt.setString(3, usuario.getPassword());
            stmt.setInt(4, usuario.getPuntos());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                System.out.println("✅ Usuario insertado correctamente.");
                resultado = true;
            }
        } catch (SQLException e) {
            System.out.println("❌ Error al insertar usuario: " + e.getMessage());
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.out.println(" Error al cerrar recursos: " + e.getMessage());   

            }
        }
               return resultado;     
    }         

            public List<Usuario> listarUsuarios() {
                List<Usuario> lista = new ArrayList<>();
                String sql = "SELECT * FROM usuarios";
                try (Connection conn = ConexionDB.conectar();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
                    
                    while (rs.next()) {
                        Usuario usuario = new Usuario(
                            rs.getInt("id_usuario"),
                            rs.getString("nombre"),
                            rs.getString("correo"),
                            rs.getString("password"),
                            rs.getInt("puntos")
                        );
                        lista.add(usuario);
                    } 
                } catch (SQLException e) {
                    System.out.println("❌ Error al listar usuarios: " + e.getMessage());
                }
                return lista;
            }

public boolean actualizarUsuario(Usuario usuario) {
    String sql = "UPDATE usuarios SET nombre=?, correo=?, password=?, puntos=? WHERE id_usuario=?";
    try (Connection conn = ConexionDB.conectar();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, usuario.getNombre());
        stmt.setString(2, usuario.getCorreo());
        stmt.setString(3, usuario.getPassword());
        stmt.setInt(4, usuario.getPuntos());
        stmt.setInt(5, usuario.getId());

        int filas = stmt.executeUpdate();
        return filas > 0;
    } catch (SQLException e) {
        System.out.println("❌ Error al actualizar usuario: " + e.getMessage());
        return false;
    }
}

public boolean eliminarUsuario(int id) {
    String sql = "DELETE FROM usuarios WHERE id_usuario=?";
    try (Connection conn = ConexionDB.conectar();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, id);
        int filas = stmt.executeUpdate();
        return filas > 0;
    } catch (SQLException e) {
        System.out.println("❌ Error al eliminar usuario: " + e.getMessage());
        return false;
    }
}

}
