<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>La Tienda de Natty | Nuevo producto</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/tienda.css">
</head>
<body>
    <main class="site-shell"><header class="site-header"><a class="brand" href="${pageContext.request.contextPath}/productos"><span class="brand-mark">N</span><span><span class="brand-name">La Tienda de Natty</span><span class="brand-tagline">Objetos para disfrutar</span></span></a><a class="header-link" href="${pageContext.request.contextPath}/productos">Volver a la tienda</a></header>
        <section class="form-panel" aria-labelledby="form-title">
            <p class="eyebrow">Gestion de catalogo</p><h1 id="form-title">Registrar producto</h1><p class="form-intro">Agrega una nueva pieza a la coleccion y cuida cada detalle de su presentacion.</p>
            <!-- Los nombres de los campos coinciden con los parámetros esperados por ProductoServlet. -->
            <form action="${pageContext.request.contextPath}/productos" method="post">
                <div class="form-grid">
                    <div class="field field-full"><label for="nombre">Nombre del producto</label><input id="nombre" type="text" name="nombre" required maxlength="100"></div>
                    <div class="field field-full"><label for="descripcion">Descripcion</label><textarea id="descripcion" name="descripcion" maxlength="500"></textarea></div>
                    <div class="field"><label for="precio">Precio</label><input id="precio" type="number" name="precio" min="0" step="0.01" required></div>
                    <div class="field"><label for="stock">Stock disponible</label><input id="stock" type="number" name="stock" min="0" step="1" required></div>
                    <div class="field"><label for="categoria">Categoria</label><input id="categoria" type="text" name="categoria" maxlength="80"></div>
                    <div class="field"><label for="foto">Foto del producto</label><input id="foto" type="file" accept="image/*" data-image-input><span class="file-help">Vista previa local. El API actual aun no persiste imagenes.</span><img class="image-preview" data-image-preview alt="Vista previa de la foto seleccionada"></div>
                </div>
                <div class="form-actions"><button class="button button-dark" type="submit">Guardar producto</button><a class="button" href="${pageContext.request.contextPath}/productos">Cancelar</a></div>
            </form>
        </section>
    </main>
    <script src="${pageContext.request.contextPath}/assets/js/tienda.js"></script>
</body>
</html>
