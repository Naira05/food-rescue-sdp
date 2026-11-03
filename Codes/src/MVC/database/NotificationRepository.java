package MVC.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {

    public void insert(String recipient, String message) {
        String sql = "INSERT INTO notifications(recipient, message, created_at) VALUES (?,?,?)";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            ps.setString(1, recipient);
            ps.setString(2, message);
            ps.setString(3, LocalDateTime.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not save notification", e);
        }
    }

    public List<String> findByRecipient(String recipient) {
        String sql = "SELECT message, created_at FROM notifications WHERE recipient = ? ORDER BY id DESC";
        try (PreparedStatement ps = Database.get().prepareStatement(sql)) {
            ps.setString(1, recipient);
            try (ResultSet rs = ps.executeQuery()) {
                List<String> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(rs.getString("created_at") + " - " + rs.getString("message"));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read notifications", e);
        }
    }
}