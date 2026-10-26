import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/kec_academic";

    private static final String USERNAME =
            System.getenv("DB_USERNAME");

    private static final String PASSWORD =
            System.getenv("DB_PASSWORD");

    public static Connection getConnection() throws SQLException {

        if (USERNAME == null || PASSWORD == null) {
            throw new SQLException(
                    "Database credentials are not configured. " +
                    "Set DB_USERNAME and DB_PASSWORD."
            );
        }

        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
