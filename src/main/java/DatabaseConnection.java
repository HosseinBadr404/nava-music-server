import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = env("NAVA_DB_URL", "jdbc:mysql://localhost:3306/nava_music");
    private static final String USER = env("NAVA_DB_USER", "nava");
    private static final String PASSWORD = env("NAVA_DB_PASSWORD", "");

    public static Connection getConnection() throws SQLException {
        if (PASSWORD.isBlank()) {
            throw new SQLException("NAVA_DB_PASSWORD is not configured");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
