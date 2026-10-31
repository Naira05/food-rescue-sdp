package MVC.controller;

import MVC.donation.Donation;
import MVC.user.Admin;
import MVC.user.User;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class AdminPanel {
    private final Admin admin;
    private final DonationController donations;
    private final UserController users;

    public AdminPanel(Admin admin, DonationController donations, UserController users) {
        this.admin = admin;
        this.donations = donations;
        this.users = users;
    }

    public Admin getAdmin() {
        return admin;
    }
    public void ensureLoggedIn() {
        requireAdmin();
    }

    //this is to manage all users like get the list or remove one
    public List<User> manageUsers() {
        requireAdmin();
        return users.getAllUsers();
    }

    public boolean removeUser(int userId) {
        requireAdmin();
        if (userId == admin.getUserID()) {
            throw new IllegalArgumentException("You cannot remove your own account");
        }
        return users.removeUser(userId);
    }

    public List<String> manageDonationTypes() {
        requireAdmin();
        return donations.getDonationTypes();
    }

    public boolean addDonationType(String name) {
        requireAdmin();
        return donations.addDonationType(name);
    }

    public boolean removeDonationType(String name) {
        requireAdmin();
        return donations.removeDonationType(name);
    }

    public List<Donation> manageDonationRecords() {
        requireAdmin();
        return donations.getDonations();
    }

    public void showDonationRecords() {
        requireAdmin();
        donations.showDonations(donations.getDonations());
    }

    public boolean deleteDonationRecord(int donationId) {
        requireAdmin();
        return donations.deleteDonation(donationId);
    }

    public Donation setDonationStatus(int donationId, Donation.Status status) {
        requireAdmin();
        return donations.updateDonation(donationId, null, null, status);
    }

    public String generateReports() {
        requireAdmin();
        List<Donation> all = donations.getDonations();

        Map<String, Integer> byStatus = new LinkedHashMap<>();
        for (Donation.Status s : Donation.Status.values()) {
            byStatus.put(s.name(), 0);
        }
        Map<String, Double> quantityByType = new TreeMap<>();

        for (Donation d : all) {
            byStatus.merge(d.getSt().name(), 1, Integer::sum);
            quantityByType.merge(d.getType() + " (" + d.getUnit() + ")", d.getQuantity(), Double::sum);
        }

        StringBuilder sb = new StringBuilder("=== Donation Report ===\n");
        sb.append("Total donations: ").append(all.size()).append('\n');
        sb.append("By status: ").append(byStatus).append('\n');
        sb.append("Quantity by type: ").append(quantityByType).append('\n');
        sb.append("Registered users: ").append(users.getAllUsers().size());
        return sb.toString();
    }

    private void requireAdmin() {
        if (!admin.isLoggedIn()) {
            throw new IllegalStateException("Admin is not logged in");
        }
    }
}
