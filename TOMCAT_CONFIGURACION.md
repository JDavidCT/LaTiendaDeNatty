# Configuración de Apache Tomcat para `LaTienda-copia`

Este documento recoge pasos rápidos en español para desplegar y probar la aplicación `LaTienda-copia` en un servidor Apache Tomcat local, además de opciones para integrar despliegues desde Maven o VS Code.

## Requisitos
- Java JDK instalado (Java 8+). Verifique con:

```bash
java -version
```

- Apache Tomcat (recomendado Tomcat 10 si usa Jakarta EE 9/10). Descargar en: https://tomcat.apache.org/
- Maven instalado (si va a compilar con Maven):

```bash
mvn -v
```

## Compatibilidad de versiones
- El `pom.xml` de este proyecto usa `jakarta.servlet-api` 5.0, lo que requiere Tomcat 10 (Jakarta EE 9+).

Si prefiere Tomcat 9 (javax.servlet), cambie la dependencia a la versión `javax.servlet` y ajuste código JSP/servlets según sea necesario.

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

## Despliegue desde VS Code (extensión Tomcat for Java)
1. Instale la extensión **Tomcat for Java** en VS Code.
2. Abra la vista Tomcat, haga clic en el botón para añadir un servidor Tomcat y seleccione la carpeta de instalación local.
3. En el proyecto, usar `mvn package` para generar el WAR y luego arrastrar el WAR desde `target` hasta el servidor en la vista Tomcat para desplegar.

## (Opcional) Snippet de plugin Maven para desplegar
Nota: algunos plugins antiguos (`tomcat7-maven-plugin`) están orientados a Tomcat 7/8/9 y pueden no ser totalmente compatibles con Tomcat 10 (Jakarta). Use este snippet sólo si usa Tomcat 9 o ha verificado compatibilidad.

Agregar en la sección `<plugins>` de `pom.xml` (opcional):

```xml
<plugin>
  <groupId>org.apache.tomcat.maven</groupId>
  <artifactId>tomcat7-maven-plugin</artifactId>
  <version>2.2</version>
  <configuration>
    <url>http://localhost:8080/manager/text</url>
    <server>tomcat-local</server>
    <path>/LaTienda-copia</path>
  </configuration>
</plugin>
```

Para usarlo, añada credenciales al archivo `settings.xml` de Maven (`<server><id>tomcat-local</id>...`) y luego:

```bash
mvn tomcat7:redeploy
```

## Notas finales y recomendaciones
- Si usa Tomcat 10 + Jakarta, mantenga la dependencia `jakarta.servlet-api` en scope `provided` (como en el `pom.xml` del proyecto).
- Para depuración rápida, despliegue en la carpeta `webapps` y revise los logs en `logs/catalina.out` o en `logs\\catalina.*.log`.
- Si quiere, puedo:
  - Añadir automáticamente el snippet del plugin al `pom.xml` (opcional).
  - Guiarle paso a paso en VS Code para instalar la extensión y desplegar.
