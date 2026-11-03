package MVC.user;

import MVC.donation.Donation;
import SingletonDP.SystemConfig;

import java.util.List;

public class Donor extends User {
    private String address;

    public Donor(int userID, String name, String email, String passwordHash, String role, String address) {
        super(userID, name, email, passwordHash, role);
        this.address = address;
    }

    // implementation of abstract methods
    @Override
    public List<String> getMenuOptions() {
        return List.of("Create donation", "My donations", "View receipt", "Edit profile",
                "Tax deduction");
    }

    @Override
    public String getRole() {
        return "Donor";
    }

    /** Deductible amount: the configured rate applied to this donor's cash donations. */
    public double calculate_tax_deduction(List<Donation> donations) {
        double cash = 0;
        for (Donation d : donations) {
            if (d.getDonorID() == getUserID() && "CASH".equalsIgnoreCase(d.getType())) {
                cash += d.getQuantity();
            }
        }
        return cash * SystemConfig.getInstance().getTaxDeductionRate();
    }

    public static Donor restore(int userId, String name, String email,
                                String passwordHash, String address) {
        Donor d = new Donor(userId, name, email, passwordHash, "Donor", address);
        return d;
    }

    // getters and setters
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
