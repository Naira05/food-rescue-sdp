package MVC.user;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Beneficiary extends User {
    private String needs;

    public record FoodRequest(String item, double quantity, LocalDate requestedOn) { }

    private final List<FoodRequest> requests = new ArrayList<>();

    public Beneficiary(int userID, String name, String email, String passwordHash, String role, String needs) {
        super(userID, name, email, passwordHash, role);
        this.needs = needs;
    }


    public static Beneficiary restore(int userId, String name, String email, String phone,
                                      String passwordHash, String needs) {
        Beneficiary b = new Beneficiary(userId, name, email, passwordHash, "Beneficiary", needs);
        b.setPhone(phone);
        return b;
    }

    // implementation of abstract methods
    @Override
    public String getRole() {
        return "Beneficiary";
    }

    @Override
    public List<String> getMenuOptions() {
        return List.of("Browse available food", "Request food", "My requests", "Edit profile");
    }

    // class methods
    public FoodRequest requestFood(String item, double quantity) {
        if (item == null || item.isBlank()) throw new IllegalArgumentException("Item is required");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        FoodRequest r = new FoodRequest(item.trim(), quantity, LocalDate.now());
        requests.add(r);
        return r;
    }

    public void restoreRequest(FoodRequest request) {
        requests.add(request);
    }

    // getters and setters
    public List<FoodRequest> getRequests() {
        return List.copyOf(requests);
    }

    public String getNeeds() {
        return needs;
    }

    public void setNeeds(String needs) {
        this.needs = needs;
    }
}
