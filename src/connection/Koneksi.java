package connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 * Singleton database connection manager with automatic reconnection
 * and user-facing error dialogs on failure.
 */
public class Koneksi {

    private static final String URL      = "jdbc:mysql://localhost:3306/cafe_pos"
                                         + "?useSSL=false&serverTimezone=Asia/Jakarta"
                                         + "&connectTimeout=5000&socketTimeout=10000";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private static Connection instance;

    /** Returns a live connection, reconnecting automatically if needed. */
    public static Connection getConnection() {
        try {
            if (instance == null || instance.isClosed() || !isAlive(instance)) {
                instance = connect();
            }
        } catch (SQLException e) {
            instance = connect();
        }
        return instance;
    }

    private static Connection connect() {
        try {
            DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            Connection c = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DB] Koneksi berhasil.");
            return c;
        } catch (SQLException e) {
            System.err.println("[DB] Koneksi gagal: " + e.getMessage());
            JOptionPane.showMessageDialog(
                null,
                "<html><b>Tidak dapat terhubung ke database!</b><br><br>"
                + "Pastikan:<br>"
                + "\u2022 MySQL server aktif (port 3306)<br>"
                + "\u2022 Database <b>cafe_pos</b> sudah dibuat<br>"
                + "\u2022 Username / password benar<br><br>"
                + "<font color='gray'>Detail: " + e.getMessage() + "</font></html>",
                "\u274C  Koneksi Gagal",
                JOptionPane.ERROR_MESSAGE
            );
            return null;
        }
    }

    private static boolean isAlive(Connection c) {
        try {
            return c.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    public static Connection requireConnection() throws SQLException {
        Connection c = getConnection();
        if (c == null) {
            throw new SQLException("Tidak terhubung ke database. Silakan restart aplikasi.");
        }
        return c;
    }
}
