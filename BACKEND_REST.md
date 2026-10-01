# Backend REST de La Tienda de Natty

El backend usa Servlets Jakarta, MySQL y JSON. El WAR se genera con Maven y está preparado para Tomcat 10.

## Preparación

1. Ejecuta `database/latienda.sql` si aún no existe la base de datos.
2. Ejecuta una vez `database/migration_rest.sql` sobre `latienda` para agregar los campos de foto y las vistas compatibles `clientes`, `compras` y `puntos`.
3. Configura en el entorno de Tomcat:

```text
LATIENDA_DB_URL=jdbc:mysql://localhost:3306/latienda?serverTimezone=America/Bogota
LATIENDA_DB_USER=usuario_mysql
LATIENDA_DB_PASSWORD=clave_mysql
NATTY_ADMIN_USER=administrador
NATTY_ADMIN_PASSWORD=clave_administrador
```

4. Ejecuta `mvn clean package` con el JDK indicado en `pom.xml` y despliega `target/LaTienda-copia.war` en Tomcat 10.

## Endpoints

- `GET /productos` y `GET /productos/{id}` son públicos.
- `POST /productos`, `PUT /productos/{id}` y `DELETE /productos/{id}` requieren la cabecera `Authorization: Bearer <token>` obtenida en `POST /api/admin/login`.
- `POST /clientes/registro` recibe `nombre`, `correo` y `contrasena`.
- `POST /clientes/login` devuelve `token` y el objeto `cliente`.
- `GET /clientes/{id}/puntos` requiere el token del cliente dueño de la cuenta o un token de administrador.
- `PUT /clientes/{id}/puntos` requiere token de administrador y recibe `{"puntos": 25}`.
- `POST /compras` requiere token del cliente y recibe `{"productos":[{"id_producto":1,"cantidad":2}]}`.
- `GET /compras/{idCliente}` requiere el token del mismo cliente.

Las fotos se reciben en el campo `fotoBase64` como URL de datos (`data:image/...;base64,...`), se almacenan como BLOB y se devuelven en `fotoUrl`. El máximo admitido es 5 MB. Las contraseñas nuevas se guardan con BCrypt; las contraseñas antiguas en texto se actualizan a BCrypt al iniciar sesión correctamente.

Las operaciones de compra usan transacción SQL, bloquean el inventario durante el cálculo, descuentan stock y suman un punto por cada $10.000 COP del total.