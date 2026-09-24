<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String idProducto = request.getParameter("id_producto");
    String nombre = request.getParameter("nombre");
    String descripcion = request.getParameter("descripcion");
    String precio = request.getParameter("precio");
    String stock = request.getParameter("stock");
    String categoria = request.getParameter("categoria");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Editar Producto</title>
</head>
<body>
    <h2>Editar Producto</h2>
    <form action="${pageContext.request.contextPath}/productos" method="post">
        <input type="hidden" name="accion" value="Actualizar">
        <input type="hidden" name="id_producto" value="<%= idProducto %>">

        <label>Nombre:</label>
        <input type="text" name="nombre" value="<%= nombre == null ? "" : nombre %>" required><br><br>

        <label>Descripción:</label>
        <textarea name="descripcion"><%= descripcion == null ? "" : descripcion %></textarea><br><br>

        <label>Precio:</label>
        <input type="number" step="0.01" name="precio" value="<%= precio == null ? "" : precio %>" required><br><br>

        <label>Stock:</label>
        <input type="number" name="stock" value="<%= stock == null ? "" : stock %>" required><br><br>

        <label>Categoría:</label>
        <input type="text" name="categoria" value="<%= categoria == null ? "" : categoria %>"><br><br>

        <button type="submit">Actualizar</button>
    </form>
    <br>
    <a href="${pageContext.request.contextPath}/productos">Volver al listado</a>
</body>
</html>
