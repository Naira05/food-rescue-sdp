package MVC.view;

import MVC.controller.AdminPanel;
import MVC.controller.DonationController;
import MVC.controller.UserController;
import MVC.database.DataAccessException;
import MVC.user.Admin;
import MVC.user.User;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** Welcome screen: sign up or log in, then open the dashboard that matches the user's role. */
public class MainMenu {
    private final UserController users;
    private final DonationController donations;
    private final ConsoleIO io;

    public MainMenu(UserController users, DonationController donations, ConsoleIO io) {
        this.users = users;
        this.donations = donations;
        this.io = io;
    }

    public void run() {
        io.println("Food Rescue Platform");
        while (true) {
            io.println("\n1. Sign up\n2. Log in\n0. Exit");
            String choice = io.prompt("Choose");
            if (choice == null || choice.trim().equals("0")) {
                return;
            }
            try {
                switch (choice.trim()) {
                    case "1" -> signUp();
                    case "2" -> logIn();
                    default -> io.println("Unknown option.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                io.println("Error: " + e.getMessage());
            } catch (DataAccessException e) {
                io.println("Database error: " + e.getMessage());
            }
        }
    }

    private void signUp() {
        io.println("Sign up as: 1. Donor  2. Volunteer  3. Beneficiary");
        String role = io.prompt("Role");
        if (role == null || !List.of("1", "2", "3").contains(role.trim())) {
            io.println("Invalid role.");
            return;
        }
        String name = io.prompt("Name");
        String email = io.prompt("E-mail");
        String phone = io.prompt("Phone (optional)");
        String password = io.prompt("Password (min 6 chars)");

        User created = switch (role.trim()) {
            case "1" -> users.registerDonor(name, email, phone, password, io.prompt("Pickup address"));
            case "2" -> {
                List<String> skills = Arrays.stream(String.valueOf(io.prompt("Skills (comma separated)")).split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).toList();
                yield users.registerVolunteer(name, email, phone, password, skills, io.prompt("Availability"));
            }
            default -> users.registerBeneficiary(name, email, phone, password, io.prompt("Needs"));
        };
        io.println("Account created: " + created + ". You can log in now.");
    }

    private void logIn() {
        String email = io.prompt("E-mail");
        String password = io.prompt("Password");
        Optional<User> result = users.login(email, password);
        if (result.isEmpty()) {
            io.println("Invalid e-mail or password.");
            return;
        }

        User user = result.get();
        io.println("Welcome, " + user.getName() + " (" + user.getRole() + ")");
        try {
            if (user instanceof Admin admin) {
                new AdminControlPanel(new AdminPanel(admin, donations, users), io).run();
            } else {
                new UserControlPanel(user, donations, users, io).run();
            }
        } finally {
            users.logout(user);
        }
        io.println("Logged out.");
    }
}
