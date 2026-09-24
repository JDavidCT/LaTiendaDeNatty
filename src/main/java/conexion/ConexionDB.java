package conexion;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.mysql.cj.jdbc.Driver;

public class ConexionDB {
    
 private static final String URL = "jdbc:mysql://localhost:3306/latienda?serverTimezone=America/Bogota";
 private static final String USER = "root"; 
 private static final String PASSWORD = "jdavidct2026";
  
    public static Connection conectar() throws SQLException {
        try {
            DriverManager.registerDriver(new Driver());
            Connection conexion = DriverManager.getConnection(URL, USER, PASSWORD);
             System.out.println("✅ Conexión exitosa a LaTienda: ");
            return conexion;
        } catch (SQLException e) {
            System.err.println("❌ Error al conectar a LaTienda: " + e.getMessage());
            throw e;
        }
    }

}
