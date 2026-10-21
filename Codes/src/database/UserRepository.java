
//this class needs so many edits
package database;
import user.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    private record Row(int id, String role, String name, String email, String hash,
                       String address, String skills, String availability, String needs) { }

    public User insert(User u) {
        String sql = "INSERT INTO users(role, name, email, password_hash, address, skills, "
                + "availability, needs) VALUES (?,?,?,?,?,?,?,?)";

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
            ps.setString(4, u.getPasswordHash());
            ps.setString(5, address);
            ps.setString(6, skills);
            ps.setString(7, availability);
            ps.setString(8, needs);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not save user", e);
        }

        return findById(lastInsertId()).orElseThrow();
    }

    public Optional<User> findById(int id) {
        return query("SELECT * FROM users WHERE id = ?", id)
                .stream().findFirst().map(this::toUser);
    }

    public Optional<User> findByEmail(String email) {
        return query("SELECT * FROM users WHERE email = ?", email)
                .stream().findFirst().map(this::toUser);
    }

    public List<User> findAll() {
        return query("SELECT * FROM users ORDER BY id")
                .stream().map(this::toUser).toList();
    }

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

    /*public void addAssignment(int volunteerId, String task) {
        execute("INSERT INTO assignments(volunteer_id, task) VALUES (?,?)",
                volunteerId, task);
    }

    private List<String> loadAssignments(int volunteerId) {
        List<String> tasks = new ArrayList<>();

        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT task FROM assignments WHERE volunteer_id = ? ORDER BY id")) {
            ps.setInt(1, volunteerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read assignments", e);
        }

        return tasks;
    }

    public void addFoodRequest(int beneficiaryId, Beneficiary.FoodRequest r) {
        execute("INSERT INTO food_requests(beneficiary_id, item, quantity, requested_on) VALUES (?,?,?,?)",
                beneficiaryId, r.item(), r.quantity(), r.requestedOn().toString());
    }

    private List<Beneficiary.FoodRequest> loadFoodRequests(int beneficiaryId) {
        List<Beneficiary.FoodRequest> requests = new ArrayList<>();

        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT item, quantity, requested_on FROM food_requests WHERE beneficiary_id = ? ORDER BY id")) {
            ps.setInt(1, beneficiaryId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    requests.add(new Beneficiary.FoodRequest(
                            rs.getString(1),
                            rs.getDouble(2),
                            LocalDate.parse(rs.getString(3))));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read food requests", e);
        }

        return requests;
    }

    private User toUser(Row r) {
        return switch (r.role()) {
            case "DONOR" -> Donor.restore(
                    r.id(), r.name(), r.email(), r.hash(), r.address());

            case "VOLUNTEER" -> {
                List<String> skills = (r.skills() == null || r.skills().isBlank())
                        ? List.of()
                        : Arrays.asList(r.skills().split(","));

                Volunteer v = Volunteer.restore(
                        r.id(), r.name(), r.email(), r.hash(),
                        skills, r.availability());

                loadAssignments(r.id()).forEach(v::assign);
                yield v;
            }

            case "BENEFICIARY" -> {
                Beneficiary b = Beneficiary.restore(
                        r.id(), r.name(), r.email(), r.hash(), r.needs());

                loadFoodRequests(r.id()).forEach(b::restoreRequest);
                yield b;
            }

            case "ADMIN" -> Admin.restore(
                    r.id(), r.name(), r.email(), r.hash());

            default -> throw new IllegalStateException(
                    "Unknown role in database: " + r.role());
        };
    }*/
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

    private void execute(String sql, Object... params) {
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Database write failed", e);
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
