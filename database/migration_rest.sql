USE `latienda`;

-- Guarda las fotos de producto directamente en MySQL (máximo 5 MB desde la API).
ALTER TABLE `productos`
    ADD COLUMN `foto` MEDIUMBLOB NULL,
    ADD COLUMN `foto_tipo` VARCHAR(40) NULL;

-- Vistas con nombres del contrato REST sobre las tablas que ya usa la aplicación.
CREATE OR REPLACE VIEW `clientes` AS
SELECT `id_usuario` AS `id_cliente`, `nombre`, `correo`, `password` AS `contrasena`,
       `fecha_registro`, `puntos`
FROM `usuarios`;

CREATE OR REPLACE VIEW `compras` AS
SELECT `id_pedidos` AS `id_compra`, `id_usuario` AS `id_cliente`, `fecha`, `total`, `estado`
FROM `pedidos`;

CREATE OR REPLACE VIEW `puntos` AS
SELECT `id_transacción` AS `id_punto`, `id_usuario` AS `id_cliente`, `puntos`, `tipo`
FROM `puntos_lealtad`;