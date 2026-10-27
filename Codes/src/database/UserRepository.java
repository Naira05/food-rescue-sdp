package database;

import user.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** All SQL for the users table. One table holds every role. */
public class UserRepository {

    /** Raw row from the users table, turned into a User after the query has closed. */
    private record Row(int id, String role, String name, String email, String phone, String hash,
                       String address, String skills, String availability, String needs) { }

    // ---- create ---------------------------------------------------------------
    /** Inserts the user and returns it again with its database-generated id. */
    public User insert(User u) {
        String sql = "INSERT INTO users(role, name, email, phone, password_hash, address, skills, "
                + "availability, needs) VALUES (?,?,?,?,?,?,?,?,?)";

        String address = null, skills = null, availability = null, needs = null;

        if (u instanceof Donor d) {
            address = d.getAddress();
        } else if (u instanceof Volunteer v) {
            skills = String.join(",", v.getSkills());
            availability = v.getAvailability();
        } else if (u instanceof Beneficiary b) {
            needs = b.getNeeds();
        }

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, roleOf(u));
            ps.setString(2, u.getName());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getPhone());
            ps.setString(5, u.getPasswordHash());
            ps.setString(6, address);
            ps.setString(7, skills);
            ps.setString(8, availability);
            ps.setString(9, needs);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not save user", e);
        }

        return findById(lastInsertId()).orElseThrow();
    }

    // ---- read -----------------------------------------------------------------
    public Optional<User> findById(int id) {
        return query("SELECT * FROM users WHERE id = ?", id)
                .stream().findFirst().map(this::toUser);
    }

    /** Case-insensitive if the e-mail column is declared COLLATE NOCASE. */
    public Optional<User> findByEmail(String email) {
        return query("SELECT * FROM users WHERE email = ?", email)
                .stream().findFirst().map(this::toUser);
    }

    public List<User> findAll() {
        return query("SELECT * FROM users ORDER BY id")
                .stream().map(this::toUser).toList();
    }

    // ---- update / delete --------------------------------------------------------
    public void updateProfile(int id, String name, String email) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE users SET name = ?, email = ? WHERE id = ?")) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not update user " + id, e);
        }
    }

    /** Fails with IllegalArgumentException if the user still has donations. */
    public boolean delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement(
                "DELETE FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (String.valueOf(e.getMessage()).toUpperCase().contains("FOREIGN KEY")) {
                throw new IllegalArgumentException("Cannot remove a user who has donations");
            }
            throw new DataAccessException("Could not delete user " + id, e);
        }
    }

    // ---- row -> object mapping ----------------------------------------------------
    private User toUser(Row r) {
        return switch (r.role()) {
            case "DONOR" -> Donor.restore(
                    r.id(), r.name(), r.email(), r.phone(), r.hash(), r.address());

            case "VOLUNTEER" -> {
                List<String> skills = (r.skills() == null || r.skills().isBlank())
                        ? List.of()
                        : Arrays.stream(r.skills().split(",")).map(String::trim).toList();
                yield Volunteer.restore(
                        r.id(), r.name(), r.email(), r.phone(), r.hash(),
                        skills, r.availability());
            }

            case "BENEFICIARY" -> Beneficiary.restore(
                    r.id(), r.name(), r.email(), r.phone(), r.hash(), r.needs());

            case "ADMIN" -> Admin.restore(
                    r.id(), r.name(), r.email(), r.phone(), r.hash());

            default -> throw new IllegalStateException(
                    "Unknown role in database: " + r.role());
        };
    }

    // ---- helpers --------------------------------------------------------------------
    private List<Row> query(String sql, Object... params) {
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<Row> rows = new ArrayList<>();

                while (rs.next()) {
                    rows.add(new Row(
                            rs.getInt("id"),
                            rs.getString("role"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("password_hash"),
                            rs.getString("address"),
                            rs.getString("skills"),
                            rs.getString("availability"),
                            rs.getString("needs")));
                }

                return rows;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read users", e);
        }
    }

    private static String roleOf(User u) {
        if (u instanceof Admin) return "ADMIN";
        if (u instanceof Donor) return "DONOR";
        if (u instanceof Volunteer) return "VOLUNTEER";
        if (u instanceof Beneficiary) return "BENEFICIARY";

        throw new IllegalArgumentException("Unknown user type: " + u.getClass());
    }

    private int lastInsertId() {
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("No generated ID returned");
        } catch (SQLException e) {
            throw new DataAccessException("Could not read generated id", e);
        }
    }

    private static Connection conn() {
        return Database.get();
    }
}