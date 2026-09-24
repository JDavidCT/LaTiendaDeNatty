package main;

import java.sql.SQLException;

import conexion.ConexionDB;
import dao.UsuarioDAO;
import modelo.Usuario;

public class Main {
    public static void main(String[] args) throws SQLException {
        // Conexión inicial
        ConexionDB.conectar();

        UsuarioDAO dao = new UsuarioDAO();

        // 1. Insertar usuario
        Usuario nuevo = new Usuario(1, "Juan", "juan@gmail.com", "clave123", 100);
        if (dao.insertarUsuario(nuevo)) {
            System.out.println("Usuario insertado correctamente.");
        }

        // 2. Listar usuarios
        System.out.println("\nLista de usuarios:");
        for (Usuario u : dao.listarUsuarios()) {
            System.out.println(u.getId() + " - " + u.getNombre() + " - " + u.getCorreo());
        }

        // 3. Actualizar usuario
        Usuario actualizado = new Usuario(1, "Juan Actualizado", "nuevoCorreo@gmail.com", "nuevaClave", 200);
        if (dao.actualizarUsuario(actualizado)) {
            System.out.println("\nUsuario actualizado correctamente.");
        }

        // Mostrar lista después de actualizar
        System.out.println("\nLista de usuarios después de actualizar:");
        for (Usuario u : dao.listarUsuarios()) {
            System.out.println(u.getId() + " - " + u.getNombre() + " - " + u.getCorreo());
        }

        // 4. Eliminar usuario
        if (dao.eliminarUsuario(1)) {
            System.out.println("\nUsuario eliminado correctamente.");
        }

        // Mostrar lista final
        System.out.println("\nLista de usuarios después de eliminar:");
        for (Usuario u : dao.listarUsuarios()) {
            System.out.println(u.getId() + " - " + u.getNombre() + " - " + u.getCorreo());
        }
    }
}
