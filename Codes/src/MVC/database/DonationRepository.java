package MVC.database;

import MVC.donation.Donation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DonationRepository {

    public Donation insert(Donation d) {
        String sql = "INSERT INTO donations(donor_id, type, description, quantity, unit, "
                + "expiry_date, donation_date, status) VALUES (?,?,?,?,?,?,?,?)";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, d.getDonorID());
            ps.setString(2, d.getType());
            ps.setString(3, d.getDescription());
            ps.setDouble(4, d.getQuantity());
            ps.setString(5, d.getUnit());
            ps.setString(6, d.getExpiryDate().toString());
            ps.setString(7, d.getDonationDate().toString());
            ps.setString(8, d.getSt().name());

            ps.executeUpdate();
            d.setDonationID(lastInsertId());

            return d;
        } catch (SQLException e) {
            throw new DataAccessException("Could not save donation", e);
        }
    }

    public Optional<Donation> findById(int id) {
        List<Donation> rows = query("SELECT * FROM donations WHERE id = ?", id);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public List<Donation> findAll() {
        return query("SELECT * FROM donations ORDER BY id");
    }

    public List<Donation> findByDonor(int donorId) {
        return query("SELECT * FROM donations WHERE donor_id = ? ORDER BY id", donorId);
    }

    public boolean update(Donation d) {
        String sql = "UPDATE donations SET description = ?, quantity = ?, status = ? WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, d.getDescription());
            ps.setDouble(2, d.getQuantity());
            ps.setString(3, d.getSt().name());
            ps.setInt(4, d.getDonationID());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not update donation " + d.getDonationID(), e);
        }
    }

    public boolean delete(int id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM donations WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete donation " + id, e);
        }
    }

    private List<Donation> query(String sql, Object... params) {
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<Donation> result = new ArrayList<>();

                while (rs.next()) {
                    result.add(map(rs));
                }

                return result;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read donations", e);
        }
    }

    private static Donation map(ResultSet rs) throws SQLException {
        return Donation.restore(
                rs.getInt("id"),
                rs.getInt("donor_id"),
                rs.getString("type"),
                rs.getDouble("quantity"),
                rs.getString("unit"),
                LocalDate.parse(rs.getString("expiry_date")),
                rs.getString("description"),
                LocalDate.parse(rs.getString("donation_date")),
                Donation.Status.of(rs.getString("status")));
    }

    private int lastInsertId() throws SQLException {
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Could not retrieve the last inserted donation ID");
        }
    }

    private static Connection conn() {
        return Database.get();
    }
}