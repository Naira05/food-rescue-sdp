package MVC.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DonationTypeRepository {

    public List<String> findAll() {
        List<String> names = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement("SELECT name FROM donation_types ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                names.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read donation types", e);
        }
        return names;
    }

    public boolean exists(String name) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT 1 FROM donation_types WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not check donation type", e);
        }
    }

    public boolean add(String name) {
        if (exists(name)) {
            return false;
        }
        try (PreparedStatement ps = conn().prepareStatement("INSERT INTO donation_types(name) VALUES (?)")) {
            ps.setString(1, name);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new DataAccessException("Could not add donation type", e);
        }
    }

    public boolean rename(String oldName, String newName) {
        if (!exists(oldName) || exists(newName)) {
            return false;
        }
        try (PreparedStatement type = conn().prepareStatement("UPDATE donation_types SET name = ? WHERE name = ?");
             PreparedStatement donations = conn().prepareStatement("UPDATE donations SET type = ? WHERE type = ?")) {
            type.setString(1, newName);
            type.setString(2, oldName);
            type.executeUpdate();
            donations.setString(1, newName);
            donations.setString(2, oldName);
            donations.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new DataAccessException("Could not rename donation type", e);
        }
    }
    public boolean remove(String name) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM donation_types WHERE name = ?")) {
            ps.setString(1, name);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not remove donation type", e);
        }
    }

    private static Connection conn() {
        return Database.get();
    }
}
