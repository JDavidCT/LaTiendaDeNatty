-- Esquema inicial de La Tienda. Ejecutar completo en MySQL 8 antes de desplegar.
CREATE DATABASE IF NOT EXISTS `latienda`
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `latienda`;

CREATE TABLE IF NOT EXISTS `usuarios` (
    `id_usuario` INT NOT NULL AUTO_INCREMENT,
    `nombre` VARCHAR(100) NOT NULL,
    `correo` VARCHAR(100) NOT NULL,
    `password` VARCHAR(100) NOT NULL,
    `fecha_registro` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    `puntos` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id_usuario`),
    UNIQUE KEY `uq_usuarios_correo` (`correo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `productos` (
    `id_producto` INT NOT NULL AUTO_INCREMENT,
    `nombre` VARCHAR(100) NOT NULL,
    `descripcion` TEXT,
    `precio` DECIMAL(10,2) NOT NULL,
    `stock` INT NOT NULL DEFAULT 0,
    `categoria` VARCHAR(50) DEFAULT NULL,
    `foto` MEDIUMBLOB NULL,
    `foto_tipo` VARCHAR(40) NULL,
    PRIMARY KEY (`id_producto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `pedidos` (
    `id_pedidos` INT NOT NULL AUTO_INCREMENT,
    `id_usuario` INT NOT NULL,
    `fecha` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    `total` DECIMAL(10,2) NOT NULL,
    `estado` ENUM('pendiente','pagado','enviado','entregado') NOT NULL DEFAULT 'pendiente',
    PRIMARY KEY (`id_pedidos`),
    KEY `ix_pedidos_usuario` (`id_usuario`),
    CONSTRAINT `fk_pedidos_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `detalle_pedido` (
    `id_detalle` INT NOT NULL AUTO_INCREMENT,
    `id_pedidos` INT NOT NULL,
    `id_producto` INT NOT NULL,
    `cantidad` INT NOT NULL,
    `precio_unitario` DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (`id_detalle`),
    KEY `ix_detalle_pedido` (`id_pedidos`),
    KEY `ix_detalle_producto` (`id_producto`),
    CONSTRAINT `fk_detalle_pedido` FOREIGN KEY (`id_pedidos`) REFERENCES `pedidos` (`id_pedidos`),
    CONSTRAINT `fk_detalle_producto` FOREIGN KEY (`id_producto`) REFERENCES `productos` (`id_producto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `puntos_lealtad` (
    `id_transaccion` INT NOT NULL AUTO_INCREMENT,
    `id_usuario` INT NOT NULL,
    `puntos` INT NOT NULL,
    `tipo` ENUM('acumulado','redimido') NOT NULL,
    PRIMARY KEY (`id_transaccion`),
    KEY `ix_puntos_usuario` (`id_usuario`),
    CONSTRAINT `fk_puntos_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

