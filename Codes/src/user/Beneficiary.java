package user;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Beneficiary extends User {
    private String needs;

    public static User restore(int id, String name, String email, String phone, String hash, String needs) {
        return null;
    }

    public record FoodRequest(String item, double quantity, LocalDate requestedOn) { }
    private final List<FoodRequest> requests = new ArrayList<>();

    public Beneficiary(int userID, String name, String email, String passwordHash, String role, String needs) {
        super(userID, name, email, passwordHash, role);
        this.needs = needs;
    }

    //implementation of abstract methods
    @Override
    public String getRole() { return "user.Beneficiary"; }
    @Override
    public List<String> getMenuOptions() {
        return List.of("Browse available food", "Request food", "My requests", "Edit profile");
    }
    //class methods
    public FoodRequest requestFood(String item, double quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        FoodRequest r = new FoodRequest(item, quantity, LocalDate.now());
        requests.add(r);
        return r;
    }
    //getters and setters
    public List<FoodRequest> getRequests() { return List.copyOf(requests); }
    public String getNeeds() { return needs; }
    public void setNeeds(String needs) { this.needs = needs; }


}
