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
            </tr>
        <%   }
           } else { %>
            <tr>
                <td colspan="6">No hay productos registrados.</td>
            </tr>
        <% } %>
    </table>
</body>
</html>
