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

    /** Returns false if a type with this name already exists. */
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

    /** Returns false if there was no type with this name. */
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
