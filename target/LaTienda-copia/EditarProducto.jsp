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
<html lang="es">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>La Tienda de Natty | Editar producto</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/tienda.css">
</head>
<body>
    <main class="site-shell"><header class="site-header"><a class="brand" href="${pageContext.request.contextPath}/productos"><span class="brand-mark">N</span><span><span class="brand-name">La Tienda de Natty</span><span class="brand-tagline">Objetos para disfrutar</span></span></a><a class="header-link" href="${pageContext.request.contextPath}/productos">Volver a la tienda</a></header>
    <section class="form-panel" aria-labelledby="edit-title"><p class="eyebrow">Gestion de catalogo</p><h1 id="edit-title">Editar producto</h1><p class="form-intro">Actualiza la informacion de esta pieza de la coleccion.</p>
    <form action="${pageContext.request.contextPath}/productos" method="post">
        <input type="hidden" name="accion" value="Actualizar">
        <input type="hidden" name="id_producto" value="<%= idProducto %>">

        <div class="form-grid"><div class="field field-full"><label for="nombre">Nombre del producto</label><input id="nombre" type="text" name="nombre" value="<%= nombre == null ? "" : nombre %>" required maxlength="100"></div>
        <div class="field field-full"><label for="descripcion">Descripcion</label><textarea id="descripcion" name="descripcion" maxlength="500"><%= descripcion == null ? "" : descripcion %></textarea></div>
        <div class="field"><label for="precio">Precio</label><input id="precio" type="number" step="0.01" min="0" name="precio" value="<%= precio == null ? "" : precio %>" required></div>
        <div class="field"><label for="stock">Stock disponible</label><input id="stock" type="number" min="0" name="stock" value="<%= stock == null ? "" : stock %>" required></div>
        <div class="field field-full"><label for="categoria">Categoria</label><input id="categoria" type="text" name="categoria" value="<%= categoria == null ? "" : categoria %>" maxlength="80"></div></div>
        <div class="form-actions"><button class="button button-dark" type="submit">Actualizar producto</button>
        <a class="button" href="${pageContext.request.contextPath}/productos">Cancelar</a></div>
    </form>
    </section></main>
    <script src="${pageContext.request.contextPath}/assets/js/tienda.js"></script>
</body>
</html>
