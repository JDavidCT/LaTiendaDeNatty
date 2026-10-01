/* Comportamiento compartido del catálogo y los formularios de producto. */
document.addEventListener('DOMContentLoaded', function () {
    var search = document.querySelector('[data-product-search]');
    var cards = Array.prototype.slice.call(document.querySelectorAll('[data-product-card]'));
    var fileInput = document.querySelector('[data-image-input]');
    var preview = document.querySelector('[data-image-preview]');

    // Filtra las tarjetas en el navegador para agilizar la consulta del catálogo.
    if (search) {
        search.addEventListener('input', function () {
            var query = search.value.toLowerCase().trim();
            cards.forEach(function (card) {
                card.hidden = query && !card.textContent.toLowerCase().includes(query);
            });
        });
    }

    // Muestra una vista previa sin subir el archivo hasta que exista soporte en la interfaz de programación.
    if (fileInput && preview) {
        fileInput.addEventListener('change', function () {
            var file = fileInput.files[0];
            if (!file) {
                preview.removeAttribute('src');
                preview.style.display = 'none';
                return;
            }
            preview.src = URL.createObjectURL(file);
            preview.style.display = 'block';
        });
    }

    // Evita eliminaciones accidentales desde el panel administrativo.
    document.querySelectorAll('[data-confirm-delete]').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            if (!window.confirm('¿Deseas eliminar este producto? Esta acción no se puede deshacer.')) {
                event.preventDefault();
            }
        });
    });

    // Simula la acción de compra hasta que exista el flujo de carrito/pago.
    document.querySelectorAll('[data-buy]').forEach(function (button) {
        button.addEventListener('click', function () {
            window.alert('Producto añadido a tu pedido. El carrito estará disponible próximamente.');
        });
    });
});
