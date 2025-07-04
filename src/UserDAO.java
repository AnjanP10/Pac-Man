
import java.sql.*;

public class UserDAO {

    public static boolean register(User user) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO users(username, password) VALUES(?, ?)");
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword()); // You can hash here later
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Registration Error: " + e.getMessage());
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean login(String username, String password) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
