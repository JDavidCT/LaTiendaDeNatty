/* Controla el inicio de sesión y las operaciones del panel administrador. */
document.addEventListener('DOMContentLoaded', function () {
    var api = window.TiendaApi;
    var tokenKey = 'natty-admin-token';
    var token = sessionStorage.getItem(tokenKey);

    function status(message, error) {
        var node = document.querySelector('[data-admin-status]') || document.querySelector('[data-login-status]');
        if (!node) return;
        node.textContent = message || '';
        node.dataset.error = error ? 'true' : 'false';
    }

    function money(value) {
        return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(Number(value) || 0);
    }

    function safe(value) {
        var element = document.createElement('span');
        element.textContent = value == null ? '' : value;
        return element.innerHTML.replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    }

    var login = document.querySelector('[data-admin-login]');
    if (login) {
        var loginError = new URLSearchParams(window.location.search).get('error');
        if (loginError) {
            status(loginError === 'config'
                ? 'El acceso de administrador no está configurado. Contacta al responsable del servidor.'
                : 'Usuario o contraseña incorrectos.', true);
        }
    }
    if (login) login.addEventListener('submit', async function (event) {
        event.preventDefault();
        var data = Object.fromEntries(new FormData(login).entries());
        try {
            var result = await api.request('api/admin/login', api.json('POST', data));
            if (!result.token) throw new Error('La respuesta no contiene una sesión válida.');
            sessionStorage.setItem(tokenKey, result.token);
            location.href = 'index.html';
        } catch (error) { status(error.message, true); }
    });

    var table = document.querySelector('[data-products-table]');
    var form = document.querySelector('[data-product-form]');
    if (!table || !form) return;

    async function loadProducts() {
        try {
            var result = await api.request('productos', { headers: token ? { 'Authorization': 'Bearer ' + token } : {} });
            var products = Array.isArray(result) ? result : result.productos || [];
            table.innerHTML = products.map(function (product) {
                var id = api.productId(product);
                var image = api.imageUrl(product);
                return '<tr><td>' + (image ? '<img class="table-image" src="' + safe(image) + '" alt="' + safe(product.nombre) + '">' : '—') + '</td><td><strong>' + safe(product.nombre) + '</strong></td><td>' + money(product.precio) + '</td><td>' + Number(product.stock || 0) + '</td><td><div class="admin-actions"><button class="button" type="button" data-edit-product="' + safe(id) + '">Editar</button><button class="button-danger" type="button" data-delete-product="' + safe(id) + '">Eliminar</button></div></td></tr>';
            }).join('') || '<tr><td colspan="5">Aún no hay productos registrados.</td></tr>';
            table.dataset.products = JSON.stringify(products);
            status('', false);
        } catch (error) { status(error.message, true); }
    }

    function resetForm() {
        form.reset();
        form.elements.id.value = '';
        form.hidden = true;
        var preview = document.querySelector('[data-image-preview]');
        preview.removeAttribute('src');
        preview.style.display = 'none';
        document.querySelector('[data-editor-title]').textContent = 'Registrar producto';
    }

    document.querySelector('[data-new-product]').addEventListener('click', function () { resetForm(); form.hidden = false; form.scrollIntoView({ behavior: 'smooth' }); });
    document.querySelector('[data-cancel-edit]').addEventListener('click', resetForm);
    document.querySelector('[data-admin-logout]').addEventListener('click', async function () {
        sessionStorage.removeItem(tokenKey);
        try { await api.request('api/admin/logout', { method: 'POST' }); }
        finally { location.href = 'login.html'; }
    });
    document.querySelector('[data-image-input]').addEventListener('change', function (event) {
        var file = event.target.files[0];
        var preview = document.querySelector('[data-image-preview]');
        if (file) { preview.src = URL.createObjectURL(file); preview.style.display = 'block'; }
    });

    form.addEventListener('submit', async function (event) {
        event.preventDefault();
        var data = Object.fromEntries(new FormData(form).entries());
        var id = data.id;
        var file = form.elements.foto.files[0];
        delete data.id;
        delete data.foto;
        data.precio = Number(data.precio);
        data.stock = Number(data.stock);
        if (file) data.fotoBase64 = await new Promise(function (resolve, reject) {
            var reader = new FileReader();
            reader.onload = function () { resolve(reader.result); };
            reader.onerror = reject;
            reader.readAsDataURL(file);
        });
        try {
            var method = id ? 'PUT' : 'POST';
            var suffix = id ? '/' + encodeURIComponent(id) : '';
            await api.request('productos' + suffix, api.json(method, data, token));
            status('Producto guardado correctamente.', false);
            resetForm();
            loadProducts();
        } catch (error) { status(error.message, true); }
    });

    table.addEventListener('click', async function (event) {
        var products = JSON.parse(table.dataset.products || '[]');
        var edit = event.target.closest('[data-edit-product]');
        if (edit) {
            var product = products.find(function (item) { return String(api.productId(item)) === edit.dataset.editProduct; });
            if (!product) return;
            ['nombre', 'categoria', 'descripcion', 'precio', 'stock'].forEach(function (key) { form.elements[key].value = product[key] == null ? '' : product[key]; });
            form.elements.id.value = api.productId(product);
            document.querySelector('[data-editor-title]').textContent = 'Editar producto';
            form.hidden = false;
            form.scrollIntoView({ behavior: 'smooth' });
        }
        var remove = event.target.closest('[data-delete-product]');
        if (remove && window.confirm('¿Deseas eliminar este producto?')) {
            try {
                await api.request('productos/' + encodeURIComponent(remove.dataset.deleteProduct), api.json('DELETE', undefined, token));
                status('Producto eliminado.', false);
                loadProducts();
            } catch (error) { status(error.message, true); }
        }
    });

    loadProducts();
});