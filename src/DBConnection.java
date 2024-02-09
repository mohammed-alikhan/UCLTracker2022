import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private Connection connection;
    private Statement statement;

    public DBConnection(String s, String root, String database12) {
    }

    public void connect() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/UCL_Tracker?serverTimezone=UTC";
        String user = "root";
        String password = "Database12";
        connection = DriverManager.getConnection(url, user, password);
        statement = connection.createStatement();
    }

    public ResultSet executeQuery(String query) throws SQLException {
        return statement.executeQuery(query);
    }

    public void close() throws SQLException {
        if (statement != null) {
            statement.close();
        }
        if (connection != null) {
            connection.close();
        }
    }

    public Connection getConnection() {
     return connection;
    }
}
