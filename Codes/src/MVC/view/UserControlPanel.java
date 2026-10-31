package MVC.view;

import MVC.controller.DonationController;
import MVC.controller.UserController;
import MVC.donation.Donation;
import MVC.user.Beneficiary;
import MVC.user.Donor;
import MVC.user.User;
import MVC.user.Volunteer;

import java.util.List;

public class UserControlPanel extends ControlPanel {
    private final User user;
    private final DonationController donations;
    private final UserController users;

    public UserControlPanel(User user, DonationController donations, UserController users, ConsoleIO io) {
        super(io);
        this.user = user;
        this.donations = donations;
        this.users = users;
    }

    @Override
    public void displayDashboard() {
        requireLogin();
        printMenu(user.getRole() + " Dashboard: " + user.getName(), user.getMenuOptions());
    }

    @Override
    protected void handleChoice(String choice) {
        requireLogin();
        if (user instanceof Donor donor) {
            donorAction(donor, choice);
        } else if (user instanceof Volunteer volunteer) {
            volunteerAction(volunteer, choice);
        } else if (user instanceof Beneficiary beneficiary) {
            beneficiaryAction(beneficiary, choice);
        } else {
            io.println("This role has no dashboard here.");
        }
    }
    private void donorAction(Donor donor, String choice) {
        switch (choice) {
            case "1" -> createDonation(donor);
            case "2" -> donations.showDonations(donations.getDonationsByDonor(donor.getUserID()));
            case "3" -> viewReceipt(donor);
            case "4" -> editProfile();
            default -> io.println("Unknown option.");
        }
    }

    private void createDonation(Donor donor) {
        donations.showDonationForm();
        io.println("Available types: " + donations.getDonationTypes());
        String type = io.prompt("Type");
        String description = io.prompt("Description");
        double quantity = io.promptDouble("Quantity");
        String unit = io.prompt("Unit");
        var expiry = io.promptDate("Expiry date (yyyy-MM-dd)");

        Donation d = donations.createDonation(donor, type, description, quantity, unit, expiry);
        io.println("Created donation #" + d.getDonationID());
    }

    private void viewReceipt(Donor donor) {
        int id = io.promptInt("Donation id");
        Donation d = donations.findDonation(id)
                .orElseThrow(() -> new IllegalArgumentException("No donation with id " + id));
        if (d.getDonorID() != donor.getUserID()) {
            throw new IllegalArgumentException("That donation belongs to another donor");
        }
        donations.showReceipt(id);
    }

    private void volunteerAction(Volunteer volunteer, String choice) {
        switch (choice) {
            case "1" -> donations.showDonations(donations.getAvailableDonations());
            case "2" -> claimPickup(volunteer);
            case "3" -> showAssignments(volunteer);
            case "4" -> editProfile();
            default -> io.println("Unknown option.");
        }
    }

    private void claimPickup(Volunteer volunteer) {
        int id = io.promptInt("Donation id to claim");
        Donation d = donations.claimDonation(id);

        String from = users.findById(d.getDonorID())
                .filter(u -> u instanceof Donor)
                .map(u -> ((Donor) u).getAddress())
                .orElse("unknown address");
        users.assignTask(volunteer, "Pick up donation #" + d.getDonationID()
                + " (" + d.getDescription() + ") from " + from);
        io.println("You claimed donation #" + d.getDonationID());
    }

    private void showAssignments(Volunteer volunteer) {
        List<String> tasks = volunteer.viewAssignment();
        io.println("---- My assignments (" + tasks.size() + ") ----");
        if (tasks.isEmpty()) io.println("(none)");
        tasks.forEach(t -> io.println("  - " + t));
    }


    private void beneficiaryAction(Beneficiary beneficiary, String choice) {
        switch (choice) {
            case "1" -> donations.showDonations(donations.getAvailableDonations());
            case "2" -> requestFood(beneficiary);
            case "3" -> showRequests(beneficiary);
            case "4" -> editProfile();
            default -> io.println("Unknown option.");
        }
    }

    private void requestFood(Beneficiary beneficiary) {
        String item = io.prompt("Item");
        double quantity = io.promptDouble("Quantity");
        io.println("Requested: " + users.requestFood(beneficiary, item, quantity));
    }

    private void showRequests(Beneficiary beneficiary) {
        List<Beneficiary.FoodRequest> requests = beneficiary.getRequests();
        io.println("---- My requests (" + requests.size() + ") ----");
        if (requests.isEmpty()) io.println("(none)");
        requests.forEach(r -> io.println("  - " + r));
    }

    private void editProfile() {
        String name = io.prompt("New name (blank = keep)");
        String email = io.prompt("New e-mail (blank = keep)");
        users.updateProfile(user, name, email);
        io.println("Profile updated: " + user);
    }

    private void requireLogin() {
        if (!user.isLoggedIn()) {
            throw new IllegalStateException("User is not logged in");
        }
    }
}
