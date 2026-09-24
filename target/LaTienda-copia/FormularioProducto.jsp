<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Registrar Producto</title>
</head>
<body>
    <h2>Registrar Producto</h2>
    <form action="${pageContext.request.contextPath}/productos" method="post">
        <label>Nombre:</label>
        <input type="text" name="nombre" required><br><br>

        <label>Descripción:</label>
        <textarea name="descripcion"></textarea><br><br>

        <label>Precio:</label>
        <input type="number" step="0.01" name="precio" required><br><br>

        <label>Stock:</label>
        <input type="number" name="stock" required><br><br>

        <label>Categoría:</label>
        <input type="text" name="categoria"><br><br>

        <input type="submit" value="Guardar">
    </form>
</body>
</html>
