package conexion;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    
 private static final String URL = "jdbc:mysql://localhost:3306/latienda?serverTimezone=America/Bogota";
 private static final String USER = "root"; 
 private static final String PASSWORD = "jdavidct2026";
  
    public static Connection conectar() {
        Connection conexion = null; 
        try {
            conexion = DriverManager.getConnection(URL, USER, PASSWORD);

             System.out.println("✅ Conexión exitosa a LaTienda: ");
        } catch (SQLException e) {
            System.out.println("❌ Error al conectar a LaTienda: " + e.getMessage());
        }
        return conexion;
    }

}
