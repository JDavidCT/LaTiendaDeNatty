/* Funciones comunes para llamar al servidor Java. */
(function () {
    var root = document.querySelector('meta[name="api-root"]');
    var base = root ? root.content : '';

    async function request(path, options) {
        var settings = options || {};
        settings.headers = Object.assign({ 'Accept': 'application/json' }, settings.headers || {});
        var response = await fetch(base + path, settings);
        var type = response.headers.get('content-type') || '';
        var body = type.includes('application/json') ? await response.json() : null;
        if (!response.ok) {
            throw new Error(body && body.mensaje ? body.mensaje : 'No fue posible completar la solicitud.');
        }
        if (!body && response.status !== 204) {
            throw new Error('El servidor no devolvió JSON. Verifica que la API REST esté disponible.');
        }
        return body;
    }

    function json(method, data, token) {
        var headers = { 'Content-Type': 'application/json' };
        if (token) headers.Authorization = 'Bearer ' + token;
        return { method: method, headers: headers, body: data === undefined ? undefined : JSON.stringify(data) };
    }

    window.TiendaApi = {
        request: request,
        json: json,
        imageUrl: function (product) { return product.fotoUrl || product.imagenUrl || product.imagen || ''; },
        productId: function (product) { return product.id || product.id_producto; }
    };
}());