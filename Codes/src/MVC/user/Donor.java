package MVC.user;

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
        return List.of("Create donation", "My donations", "View receipt", "Edit profile");
    }

    @Override
    public String getRole() {
        return "Donor";
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
