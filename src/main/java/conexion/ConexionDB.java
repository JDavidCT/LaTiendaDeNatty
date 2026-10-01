package conexion;
import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ConexionDB {
    private static final String DATA_SOURCE_NAME = "java:comp/env/jdbc/LaTiendaDB";

    public static Connection conectar() throws SQLException {
        try {
            DataSource dataSource = (DataSource) new InitialContext().lookup(DATA_SOURCE_NAME);
            return dataSource.getConnection();
        } catch (NamingException e) {
            throw new SQLException("No se encontró el datasource JNDI " + DATA_SOURCE_NAME + ".", e);
        }
    }
}
