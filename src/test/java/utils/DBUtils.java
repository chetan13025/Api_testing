package utils;

import java.sql.*;

public class DBUtils {
    private static String url = "jdbc:mysql://localhost:3306/library"; // change DB name
    private static String username = "root"; // your DB username
    private static String password = "root"; // your DB password

    public static ResultSet getBookById(int bookId) throws Exception {
        Connection conn = DriverManager.getConnection(url, username, password);
        Statement stmt = conn.createStatement();
        String query = "SELECT * FROM books WHERE id=" + bookId;
        return stmt.executeQuery(query);
    }
}
