package MVC.user;

import SingletonDP.SystemConfig;

import java.util.ArrayList;
import java.util.List;

public class Volunteer extends User {
    private final List<String> skills = new ArrayList<>();
    private String availability;
    private final List<String> assignments = new ArrayList<>();

    public Volunteer(int userID, String name, String email, String passwordHash, String role,
                     String availability, List<String> skills) {
        super(userID, name, email, passwordHash, role);
        this.availability = availability;
        this.skills.addAll(skills);
    }

    // implementation of abstract methods
    @Override
    public String getRole() {
        return "Volunteer";
    }

    @Override
    public List<String> getMenuOptions() {
        return List.of("Available pickups", "Claim a pickup", "My assignments", "Edit profile",
                "View certificate");
    }

    // implementation of the class methods

    public void assign(String task) {
        assignments.add(task);
    }

    public List<String> viewAssignment() {
        return List.copyOf(assignments);
    }

    /** Builds a certificate from the volunteer's completed assignments. */
    public String viewCertificate() {
        return "===== CERTIFICATE OF APPRECIATION =====\n"
                + SystemConfig.getInstance().getOrganizationName() + " thanks " + getName() + "\n"
                + "for completing " + assignments.size() + " assignment(s).\n"
                + "Skills: " + (skills.isEmpty() ? "-" : String.join(", ", skills)) + "\n"
                + "=======================================";
    }

    public static Volunteer restore(int userId, String name, String email,
                                    String passwordHash, List<String> skills, String availability) {
        Volunteer v = new Volunteer(userId, name, email, passwordHash, "Volunteer", availability, skills);
        return v;
    }

    // getters and setters
    public List<String> getSkills() {
        return List.copyOf(skills);
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }
}
