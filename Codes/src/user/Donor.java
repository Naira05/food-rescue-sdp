package user;
import controller.DonationController;
import donation.Donation;

import java.util.List;

public class Donor extends User{
    private String address;

    public Donor(int userID, String name, String email, String passwordHash, String role, String address) {
        super(userID, name, email, passwordHash, role);
        this.address = address;
    }

    //implementation of abstract methods
    @Override
    public List<String> getMenuOptions() {
        return List.of("Create donation", "My donations", "View receipt", "Edit profile");
    }
    @Override
    public String getRole(){
        return "Donor";
    }

    //implementation of class methods
    //public List<Donation> viewDonations(DonationController controller) {}

    //getters and setters
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
}
