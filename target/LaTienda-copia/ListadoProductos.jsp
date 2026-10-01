<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="modelo.Producto" %>
<%
    List<Producto> productos = (List<Producto>) request.getAttribute("productos");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>La Tienda de Natty | Catalogo</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/tienda.css">
</head>
<body>
    <main class="site-shell">
        <header class="site-header">
            <a class="brand" href="${pageContext.request.contextPath}/productos" aria-label="La Tienda de Natty, inicio">
                <span class="brand-mark">N</span>
                <span><span class="brand-name">La Tienda de Natty</span><span class="brand-tagline">Objetos para disfrutar</span></span>
            </a>
        </header>

        <section class="content-width hero" aria-labelledby="page-title">
            <div>
                <p class="eyebrow">Coleccion actual</p>
                <h1 id="page-title">Pequenas cosas, grandes momentos.</h1>
                <p class="lead">Una seleccion curada para llenar tus espacios de calidez, textura y personalidad.</p>
            </div>
            <p class="hero-note">Compra con calma. Cada producto merece un lugar especial en tu historia.</p>
        </section>

        <section class="content-width" aria-labelledby="catalog-title">
            <div class="toolbar">
                <h2 id="catalog-title">Nuestros favoritos</h2>
                <input class="search" type="search" data-product-search placeholder="Buscar en la tienda..." aria-label="Buscar productos">
            </div>
            <div class="product-grid">
                <% if (productos != null && !productos.isEmpty()) {
                       for (Producto p : productos) { %>
                    <article class="product-card" data-product-card>
                        <div class="product-art" aria-hidden="true"><%= p.getNombre() == null || p.getNombre().isEmpty() ? "N" : p.getNombre().substring(0, 1).toUpperCase() %></div>
                        <div class="product-body">
                            <span class="product-category"><%= p.getCategoria() == null ? "Coleccion" : p.getCategoria() %></span>
                            <h3 class="product-name"><%= p.getNombre() %></h3>
                            <p class="product-description"><%= p.getDescripcion() == null ? "Una pieza especial para tu espacio." : p.getDescripcion() %></p>
                            <div class="product-footer">
                                <span><span class="price">$<%= String.format("%,.2f", p.getPrecio()) %></span><br><span class="stock"><%= p.getStock() %> disponibles</span></span>
                                <button class="button" type="button" data-buy>Comprar</button>
                            </div>
                        </div>
                    </article>
                <%   }
                   } else { %>
                    <p class="empty-state">Aun no hay productos en la coleccion.</p>
                <% } %>
            </div>
        </section>

    </main>
    <script src="${pageContext.request.contextPath}/assets/js/tienda.js"></script>
</body>
</html>
