package servlets;

import dao.ProductoDAO;
import modelo.Producto;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Servlet para gestionar productos de la tienda.
 * Maneja las operaciones de listado e inserción.
 */
@WebServlet("/productos")
public class ProductoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ProductoDAO dao = new ProductoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Obtener lista de productos desde la BD
            List<Producto> lista = dao.listar();
            // Pasar la lista como atributo a la vista JSP
            request.setAttribute("productos", lista);
            // Redirigir al JSP de listado
            request.getRequestDispatcher("ListadoProductos.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Error al listar productos", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Capturar datos del formulario
        String nombre = request.getParameter("nombre");
        String descripcion = request.getParameter("descripcion");
        double precio = Double.parseDouble(request.getParameter("precio"));
        int stock = Integer.parseInt(request.getParameter("stock"));
        String categoria = request.getParameter("categoria");

        // Crear objeto Producto
        Producto p = new Producto(0, nombre, descripcion, precio, stock, categoria);

        try {
            // Insertar en la BD
            dao.insertar(p);
            // Redirigir al listado
            response.sendRedirect("productos");
        } catch (SQLException e) {
            throw new ServletException("Error al insertar producto", e);
        }
    }
}
