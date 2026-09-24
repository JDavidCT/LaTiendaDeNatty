<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="modelo.Producto" %>
<%
    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Listado de Productos</title>
</head>
<body>
    <h2>Listado de Productos</h2>
    <a href="FormularioProducto.jsp">Registrar nuevo producto</a>
    <br><br>
    <table border="1" cellpadding="5">
        <tr>
            <th>ID</th>
            <th>Nombre</th>
            <th>Descripción</th>
            <th>Precio</th>
            <th>Stock</th>
            <th>Categoría</th>
            <th>Acciones</th>
        </tr>
        <% if (productos != null) {
               for (Producto p : productos) { %>
            <tr>
                <td><%= p.getId() %></td>
                <td><%= p.getNombre() %></td>
                <td><%= p.getDescripcion() %></td>
                <td><%= p.getPrecio() %></td>
                <td><%= p.getStock() %></td>
                <td><%= p.getCategoria() %></td>
                <td>
                    <a href="EditarProducto.jsp?id_producto=<%= p.getId() %>&amp;nombre=<%= java.net.URLEncoder.encode(p.getNombre(), "UTF-8") %>&amp;descripcion=<%= java.net.URLEncoder.encode(p.getDescripcion() == null ? "" : p.getDescripcion(), "UTF-8") %>&amp;precio=<%= p.getPrecio() %>&amp;stock=<%= p.getStock() %>&amp;categoria=<%= java.net.URLEncoder.encode(p.getCategoria() == null ? "" : p.getCategoria(), "UTF-8") %>">Editar</a>
                    <form action="${pageContext.request.contextPath}/productos" method="post" style="display:inline; margin-left:10px;">
                        <input type="hidden" name="accion" value="Eliminar">
                        <input type="hidden" name="id_producto" value="<%= p.getId() %>">
                        <button type="submit">Eliminar</button>
                    </form>
                </td>
            </tr>
        <%   }
           } else { %>
            <tr>
                <td colspan="7">No hay productos registrados.</td>
            </tr>
        <% } %>
    </table>
</body>
</html>
