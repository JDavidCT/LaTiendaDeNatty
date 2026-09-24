package servlets;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

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
        String accion = request.getParameter("accion");
        try {
            if ("Eliminar".equals(accion)) {
                int id = Integer.parseInt(request.getParameter("id_producto"));
                dao.eliminarProducto(id);
            } else {
                String nombre = request.getParameter("nombre");
                String descripcion = request.getParameter("descripcion");
                double precio = Double.parseDouble(request.getParameter("precio"));
                int stock = Integer.parseInt(request.getParameter("stock"));
                String categoria = request.getParameter("categoria");
                int id = "Actualizar".equals(accion)
                        ? Integer.parseInt(request.getParameter("id_producto"))
                        : 0;
                Producto producto = new Producto(id, nombre, descripcion, precio, stock, categoria);

                if ("Actualizar".equals(accion)) {
                    dao.actualizarProducto(producto);
                } else {
                    dao.insertar(producto);
                }
            }
            response.sendRedirect("productos");
        } catch (NumberFormatException e) {
            throw new ServletException("Datos numéricos del producto no válidos", e);
        } catch (SQLException e) {
            throw new ServletException("Error al guardar el producto", e);
        }
    }
}
