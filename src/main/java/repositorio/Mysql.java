package repositorio;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Mysql {
    private static Connection connection;

    public static Connection getConnection() {
        //  variáveis de ambiente
        String url = System.getenv("DB_URL"); 
        String user = System.getenv("DB_USER"); 
        String senha = System.getenv("DB_PASSWORD"); 

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, user, senha);
            return connection;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("Erro ao conectar");
        } catch (ClassNotFoundException e) {
            System.out.println("Erro ao importar o driver");
        }
        return null;
    }
}
