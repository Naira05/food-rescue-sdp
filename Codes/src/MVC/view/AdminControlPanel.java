package MVC.view;

import MVC.controller.AdminPanel;
import MVC.donation.Donation;

import java.util.List;

public class AdminControlPanel extends ControlPanel {
    private final AdminPanel admin;

    public AdminControlPanel(AdminPanel admin, ConsoleIO io) {
        super(io);
        this.admin = admin;
    }

    @Override
    public void displayDashboard() {
        admin.ensureLoggedIn(); // a logged-out admin cannot see the menu
        printMenu("Admin Dashboard: " + admin.getAdmin().getName(), admin.getAdmin().getMenuOptions());
    }

    @Override
    protected void handleChoice(String choice) {
        switch (choice) {
            case "1" -> admin.manageUsers().forEach(u -> io.println("  " + u));
            case "2" -> io.println(admin.removeUser(io.promptInt("User id")) ? "User removed." : "No such user.");
            case "3" -> io.println("  Types: " + admin.manageDonationTypes());
            case "4" -> io.println(admin.addDonationType(io.prompt("New type")) ? "Added." : "Already exists.");
            case "5" -> io.println(admin.removeDonationType(io.prompt("Type to remove")) ? "Removed." : "No such type.");
            case "6" -> admin.showDonationRecords();
            case "7" -> updateStatus();
            case "8" -> io.println(admin.deleteDonationRecord(io.promptInt("Donation id")) ? "Deleted." : "No such donation.");
            case "9" -> io.println(admin.generateReports());
            default -> io.println("Unknown option.");
        }
    }

    private void updateStatus() {
        int id = io.promptInt("Donation id");
        List<Donation.Status> options = Donation.Status.values();
        String text = io.prompt("Status " + options);
        Donation.Status status = Donation.Status.of(String.valueOf(text).trim().toUpperCase());
        Donation d = admin.setDonationStatus(id, status);
        io.println("Updated: " + d);
    }
}
