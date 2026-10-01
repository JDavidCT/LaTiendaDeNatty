# Configuración de Apache Tomcat para `LaTienda-copia`

Este documento recoge pasos rápidos en español para desplegar y probar la aplicación `LaTienda-copia` en un servidor Apache Tomcat local, además de opciones para integrar despliegues desde Maven o VS Code.

## Requisitos
- Java JDK 17 o superior. Verifique con:

```bash
java -version
```

- Apache Tomcat 10.1 (Jakarta Servlet 6). Descargar en: https://tomcat.apache.org/
- Maven instalado (si va a compilar con Maven):

```bash
mvn -v
```

## Base de datos y credenciales
1. Inicie MySQL y ejecute `database/setup.sql` completo.
2. En `src/main/webapp/META-INF/context.xml`, reemplace `username="root"` y `password=""` por las credenciales de su usuario MySQL.
3. El panel usa un único administrador configurado fuera de MySQL. No requiere tabla de administradores. Genere un hash BCrypt con la biblioteca que se empaqueta en el WAR:

```powershell
jshell --class-path target\LaTienda-copia\WEB-INF\lib\jbcrypt-0.4.jar
```

En JShell, genere y copie el hash (no use la contraseña en texto plano como valor de variable):

```java
import org.mindrot.jbcrypt.BCrypt;
BCrypt.hashpw("SU_CONTRASENA", BCrypt.gensalt(12))
```

El inicio con el script solicita el nombre de usuario y ese hash BCrypt. Para iniciar Tomcat desde CMD directamente, defina ambas variables primero:

```bat
set LATIENDA_ADMIN_USER=admin
set LATIENDA_ADMIN_PASSWORD_HASH=$2a$12$REEMPLACE_POR_EL_HASH_GENERADO
%CATALINA_HOME%\bin\catalina.bat run
```

La contraseña no se guarda en MySQL ni en el código fuente. Reinicie Tomcat cuando cambie estas variables.

## Iniciar Tomcat
El script solicita la carpeta de Tomcat y las credenciales únicas del administrador. La conexión de datos de la aplicación se obtiene del datasource JNDI configurado en `META-INF/context.xml`:

```powershell
Set-Location "$env:USERPROFILE\Desktop\LaTienda-copia"
powershell -ExecutionPolicy Bypass -File .\iniciar-tomcat.ps1
```

Si el puerto 8080 ya está ocupado, detenga la instancia activa antes de volver a ejecutar el script.

## Despliegue manual rápido
1. Compilar el WAR:

```bash
mvn clean package
```

2. Copiar el archivo generado `target/LaTienda-copia.war` a la carpeta `webapps` de Tomcat. Por ejemplo (Windows):

```powershell
copy target\LaTienda-copia.war "C:\path\to\apache-tomcat-10\webapps\"
```

3. Iniciar Tomcat (o reiniciarlo). Acceder a:

```
http://localhost:8080/LaTienda-copia/
```

Clientes: `/LaTienda-copia/index.html`. Administración: `/LaTienda-copia/admin/index.html`.

## Despliegue desde VS Code (extensión Tomcat for Java)
1. Instale la extensión **Tomcat for Java** en VS Code.
2. Abra la vista Tomcat, haga clic en el botón para añadir un servidor Tomcat y seleccione la carpeta de instalación local.
3. En el proyecto, usar `mvn package` para generar el WAR y luego arrastrar el WAR desde `target` hasta el servidor en la vista Tomcat para desplegar.

## Notas finales y recomendaciones
- Mantenga `jakarta.servlet-api` en scope `provided`; el driver MySQL se empaqueta dentro del WAR.
- Para depuración rápida, despliegue en la carpeta `webapps` y revise los logs en `logs/catalina.out` o en `logs\\catalina.*.log`.
