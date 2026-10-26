package biblioteca.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionFactory {
    private ConnectionFactory() { }

    public static Connection abrir() throws SQLException {
        String url = System.getenv("BIBLIOTECA_DB_URL");
        String user = System.getenv("BIBLIOTECA_DB_USER");
        String password = System.getenv("BIBLIOTECA_DB_PASSWORD");
        if (url == null || user == null || password == null) {
            throw new SQLException("Configure BIBLIOTECA_DB_URL, BIBLIOTECA_DB_USER e BIBLIOTECA_DB_PASSWORD.");
        }
        return DriverManager.getConnection(url, user, password);
    }
}
