/* Controla el catálogo, la cuenta de cliente y el carrito. */
document.addEventListener('DOMContentLoaded', function () {
    var api = window.TiendaApi;
    var page = document.body.dataset.page;
    var cartKey = 'natty-carrito';
    var customerKey = 'natty-cliente';
    var products = [];

    function setStatus(node, message, error) {
        if (!node) return;
        node.textContent = message || '';
        node.dataset.error = error ? 'true' : 'false';
    }

    function customer() {
        try { return JSON.parse(sessionStorage.getItem(customerKey) || 'null'); } catch (_) { return null; }
    }

    function cart() {
        try { return JSON.parse(localStorage.getItem(cartKey) || '[]'); } catch (_) { return []; }
    }

    function saveCart(value) {
        localStorage.setItem(cartKey, JSON.stringify(value));
        document.querySelectorAll('[data-cart-count]').forEach(function (node) {
            node.textContent = value.reduce(function (sum, item) { return sum + item.cantidad; }, 0);
        });
    }

    function money(value) {
        return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(Number(value) || 0);
    }

    function safe(value) {
        var node = document.createElement('span');
        node.textContent = value == null ? '' : value;
        return node.innerHTML.replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    }

    function productImage(product, className) {
        var url = api.imageUrl(product);
        return url ? '<img class="' + className + '" src="' + safe(url) + '" alt="' + safe(product.nombre) + '">' : '<div class="' + className + ' product-art" role="img" aria-label="Imagen no disponible">N</div>';
    }

    async function loadProducts() {
        var response = await api.request('productos');
        products = Array.isArray(response) ? response : response.productos || [];
        return products;
    }

    function renderCatalog() {
        var grid = document.querySelector('[data-product-grid]');
        if (!grid) return;
        var query = (document.querySelector('[data-product-search]') || {}).value || '';
        var visible = products.filter(function (item) {
            return (item.nombre + ' ' + (item.categoria || '')).toLowerCase().includes(query.toLowerCase().trim());
        });
        grid.innerHTML = visible.map(function (product) {
            var id = encodeURIComponent(api.productId(product));
            return '<article class="product-card">' + productImage(product, 'catalog-image') + '<div class="product-body"><span class="product-category">' + safe(product.categoria || 'Colección') + '</span><h3 class="product-name">' + safe(product.nombre) + '</h3><p class="product-description">' + safe(product.descripcion || '') + '</p><div class="product-footer"><span><span class="price">' + money(product.precio) + '</span><br><span class="stock">' + Number(product.stock || 0) + ' disponibles</span></span><a class="button" href="cliente/detalle.html?id=' + id + '">Ver detalles</a></div><button class="button button-dark catalog-actions" type="button" data-add-cart="' + id + '">Agregar al carrito</button></div></article>';
        }).join('') || '<p class="empty-state">No encontramos productos para esta búsqueda.</p>';
    }

    function renderCart() {
        var target = document.querySelector('[data-cart-items]');
        if (!target) return;
        var items = cart();
        target.innerHTML = items.map(function (item) {
            return '<div class="cart-row"><span>' + safe(item.nombre) + '<br><span class="stock">' + money(item.precio) + ' cada uno</span></span><input type="number" min="1" max="' + item.stock + '" value="' + item.cantidad + '" aria-label="Cantidad de ' + safe(item.nombre) + '" data-cart-quantity="' + item.id + '"><button class="button-danger cart-remove" type="button" data-remove-cart="' + item.id + '">Quitar</button></div>';
        }).join('') || '<p class="empty-state">Tu carrito está vacío.</p>';
        var total = items.reduce(function (sum, item) { return sum + item.precio * item.cantidad; }, 0);
        document.querySelector('[data-cart-total]').textContent = money(total);
        saveCart(items);
    }

    function addToCart(id) {
        var product = products.find(function (item) { return String(api.productId(item)) === String(id); });
        if (!product || Number(product.stock) < 1) return;
        var items = cart();
        var line = items.find(function (item) { return String(item.id) === String(id); });
        if (line) line.cantidad = Math.min(line.cantidad + 1, Number(product.stock));
        else items.push({ id: api.productId(product), nombre: product.nombre, precio: Number(product.precio), stock: Number(product.stock), cantidad: 1 });
        saveCart(items);
        renderCart();
        setStatus(document.querySelector('[data-cart-status]'), 'Producto agregado al carrito.');
    }

    async function renderDetail() {
        var host = document.querySelector('[data-product-detail]');
        if (!host) return;
        try {
            await loadProducts();
            var id = new URLSearchParams(location.search).get('id');
            var item = products.find(function (product) { return String(api.productId(product)) === String(id); });
            if (!item) throw new Error('No encontramos este producto.');
            host.innerHTML = productImage(item, 'detail-image') + '<div class="detail-copy"><p class="eyebrow">' + safe(item.categoria || 'Colección') + '</p><h1 class="product-name">' + safe(item.nombre) + '</h1><p class="product-description">' + safe(item.descripcion || '') + '</p><p class="price">' + money(item.precio) + '</p><p class="stock">' + Number(item.stock || 0) + ' disponibles</p><button class="button button-dark" type="button" data-detail-add="' + safe(id) + '">Agregar al carrito</button></div>';
        } catch (error) { setStatus(host.querySelector('[data-detail-status]') || host.appendChild(document.createElement('p')), error.message, true); }
    }

    async function submitAccount(form, register) {
        var data = Object.fromEntries(new FormData(form).entries());
        var status = document.querySelector('[data-account-status]');
        try {
            var result = await api.request(register ? 'clientes/registro' : 'clientes/login', api.json('POST', data));
            if (!register) {
                sessionStorage.setItem(customerKey, JSON.stringify(result.cliente || result));
                renderAccount();
            } else {
                setStatus(status, 'Cuenta creada. Ya puedes iniciar sesión.');
                form.reset();
                document.querySelector('[data-account-tab="login"]').click();
            }
        } catch (error) { setStatus(status, error.message, true); }
    }

    function renderAccount() {
        var profile = customer();
        var summary = document.querySelector('[data-account-summary]');
        if (!summary) return;
        summary.hidden = !profile;
        document.querySelectorAll('.account-form').forEach(function (form) { form.hidden = Boolean(profile) || form !== document.querySelector('[data-customer-login]'); });
        if (profile) {
            document.querySelector('[data-customer-name]').textContent = profile.nombre || profile.name || '';
            document.querySelector('[data-customer-points]').textContent = Number(profile.puntos || profile.points || 0);
            api.request('clientes/' + encodeURIComponent(profile.id) + '/puntos', {
                headers: { 'Accept': 'application/json', 'Authorization': 'Bearer ' + profile.token }
            }).then(function (result) {
                profile.puntos = result.puntos;
                sessionStorage.setItem(customerKey, JSON.stringify(profile));
                document.querySelector('[data-customer-points]').textContent = Number(result.puntos || 0);
            }).catch(function (error) { setStatus(document.querySelector('[data-account-status]'), error.message, true); });
        }
    }

    document.addEventListener('click', function (event) {
        var add = event.target.closest('[data-add-cart]');
        if (add) addToCart(decodeURIComponent(add.dataset.addCart));
        var detailAdd = event.target.closest('[data-detail-add]');
        if (detailAdd) loadProducts().then(function () { addToCart(detailAdd.dataset.detailAdd); });
        var remove = event.target.closest('[data-remove-cart]');
        if (remove) { saveCart(cart().filter(function (item) { return String(item.id) !== remove.dataset.removeCart; })); renderCart(); }
        var openCart = event.target.closest('[data-open-cart]');
        if (openCart) { renderCart(); document.querySelector('[data-cart-dialog]').showModal(); }
        if (event.target.closest('[data-close-cart]')) document.querySelector('[data-cart-dialog]').close();
        var tab = event.target.closest('[data-account-tab]');
        if (tab) {
            document.querySelector('[data-customer-login]').hidden = tab.dataset.accountTab !== 'login';
            document.querySelector('[data-customer-register]').hidden = tab.dataset.accountTab !== 'register';
        }
        if (event.target.closest('[data-customer-logout]')) { sessionStorage.removeItem(customerKey); renderAccount(); }
    });

    document.addEventListener('input', function (event) {
        if (event.target.matches('[data-product-search]')) renderCatalog();
        if (event.target.matches('[data-cart-quantity]')) {
            var id = event.target.dataset.cartQuantity;
            var items = cart();
            var line = items.find(function (item) { return String(item.id) === id; });
            if (line) line.cantidad = Math.max(1, Math.min(Number(event.target.value), line.stock));
            saveCart(items);
            renderCart();
        }
    });

    document.addEventListener('submit', function (event) {
        if (event.target.matches('[data-customer-login]')) { event.preventDefault(); submitAccount(event.target, false); }
        if (event.target.matches('[data-customer-register]')) { event.preventDefault(); submitAccount(event.target, true); }
    });

    var purchase = document.querySelector('[data-confirm-purchase]');
    if (purchase) purchase.addEventListener('click', async function () {
        var profile = customer();
        var items = cart();
        var status = document.querySelector('[data-cart-status]');
        if (!profile) { setStatus(status, 'Inicia sesión para confirmar tu compra.', true); return; }
        if (!items.length) { setStatus(status, 'Agrega productos antes de confirmar la compra.', true); return; }
        try {
            var result = await api.request('compras', api.json('POST', { productos: items.map(function (item) { return { id_producto: item.id, cantidad: item.cantidad }; }) }, profile.token));
            saveCart([]);
            renderCart();
            profile.puntos = result.puntos != null ? result.puntos : profile.puntos;
            sessionStorage.setItem(customerKey, JSON.stringify(profile));
            setStatus(status, 'Compra confirmada. Puntos acumulados: ' + Number(profile.puntos || 0) + '.');
        } catch (error) { setStatus(status, error.message, true); }
    });

    document.querySelectorAll('[data-customer-logout]').forEach(function (button) { button.addEventListener('click', function () { sessionStorage.removeItem(customerKey); renderAccount(); }); });
    if (page === 'cliente') {
        loadProducts().then(renderCatalog).catch(function (error) { setStatus(document.querySelector('[data-catalog-status]'), error.message, true); });
        saveCart(cart());
    }
    if (page === 'detalle') renderDetail();
    if (page === 'perfil') renderAccount();
});