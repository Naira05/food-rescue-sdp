//this implementation is not complete yet


package controller;
import user.Admin;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class AdminPanel extends ControlPanel {
    private final Admin admin;
    private final Set<String> donationTypes = new LinkedHashSet<>(
            List.of("Cooked meals", "Produce", "Packaged food"));

    public AdminPanel(Admin admin, DonationController controller) {
        super(controller);
        this.admin = admin;
    }

    @Override
    public void displayDashboard() {
        //calling a function to get if the admin is logged in
        System.out.println("\n=== Admin Dashboard: " + admin.getName() + " ===");
        List<String> options = admin.getMenuOptions();
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ". " + options.get(i));
        }
        System.out.println("0. Log out");
    }

}