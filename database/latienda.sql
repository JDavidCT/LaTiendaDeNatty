-- --------------------------------------------------------
-- Host:                         localhost
-- Versión del servidor:         8.0.45 - MySQL Community Server - GPL
-- SO del servidor:              Win64
-- HeidiSQL Versión:             12.17.0.7270
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Volcando estructura de base de datos para latienda
CREATE DATABASE IF NOT EXISTS `latienda` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `latienda`;

-- Volcando estructura para tabla latienda.detalle_pedido
CREATE TABLE IF NOT EXISTS `detalle_pedido` (
  `id_detalle` int NOT NULL AUTO_INCREMENT,
  `id_pedidos` int NOT NULL,
  `id_producto` int NOT NULL,
  `cantidad` int NOT NULL,
  `precio_unitario` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id_detalle`),
  KEY `id_pedidos` (`id_pedidos`),
  KEY `id_producto` (`id_producto`),
  CONSTRAINT `detalle_pedido_ibfk_1` FOREIGN KEY (`id_pedidos`) REFERENCES `pedidos` (`id_pedidos`),
  CONSTRAINT `detalle_pedido_ibfk_2` FOREIGN KEY (`id_producto`) REFERENCES `productos` (`id_producto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Volcando datos para la tabla latienda.detalle_pedido: ~0 rows (aproximadamente)

-- Volcando estructura para tabla latienda.pedidos
CREATE TABLE IF NOT EXISTS `pedidos` (
  `id_pedidos` int NOT NULL AUTO_INCREMENT,
  `id_usuario` int NOT NULL,
  `fecha` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `total` decimal(10,2) NOT NULL,
  `estado` enum('pendiente','pagado','enviado','entregado') DEFAULT 'pendiente',
  PRIMARY KEY (`id_pedidos`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `pedidos_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Volcando datos para la tabla latienda.pedidos: ~0 rows (aproximadamente)

-- Volcando estructura para tabla latienda.productos
CREATE TABLE IF NOT EXISTS `productos` (
  `id_producto` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `descripción` text,
  `precio` decimal(10,2) NOT NULL,
  `stock` int DEFAULT '0',
  `categoria` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id_producto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Volcando datos para la tabla latienda.productos: ~0 rows (aproximadamente)

-- Volcando estructura para tabla latienda.puntos_lealtad
CREATE TABLE IF NOT EXISTS `puntos_lealtad` (
  `id_transacción` int NOT NULL AUTO_INCREMENT,
  `id_usuario` int NOT NULL,
  `puntos` int NOT NULL,
  `tipo` enum('acumulado','redimido') NOT NULL,
  PRIMARY KEY (`id_transacción`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `puntos_lealtad_ibfk_1` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Volcando datos para la tabla latienda.puntos_lealtad: ~0 rows (aproximadamente)

-- Volcando estructura para tabla latienda.usuarios
CREATE TABLE IF NOT EXISTS `usuarios` (
  `id_usuario` int NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `correo` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `fecha_registro` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `puntos` int DEFAULT '0',
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `correo` (`correo`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Volcando datos para la tabla latienda.usuarios: ~0 rows (aproximadamente)
INSERT INTO `usuarios` (`id_usuario`, `nombre`, `correo`, `password`, `fecha_registro`, `puntos`) VALUES
	(1, 'Juan Tabares', 'JuanT@gmail.com', 'clave456', '2026-04-24 22:13:03', 100);

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
