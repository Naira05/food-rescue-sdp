package user;

import java.util.ArrayList;
import java.util.List;

public class Volunteer extends User{
    private final List<String> skills = new ArrayList<>();
    private String availability;
    private final List<String> assignments = new ArrayList<>();

    public Volunteer(int userID, String name, String email, String passwordHash, String role, String availability, List<String> skills) {
        super(userID, name, email, passwordHash, role);
        this.availability = availability;
        this.skills.addAll(skills);
    }

    //implementation of abstract methods
    @Override
    public String getRole(){return "user.Volunteer";}
    @Override
    public List<String> getMenuOptions(){
        return List.of("Available pickups", "Claim a pickup", "My assignments", "Edit profile");
    }

    //implementation of the class methods
    public void assign(String task){assignments.add(task);}
    public List<String> viewAssignment(){
    return List.copyOf(assignments);
    }
    //getters and setters
    public List<String> getSkills() {
        return skills;
    }
    public String getAvailability() {
        return availability;
    }
    public void setAvailability(String availability) {
        this.availability = availability;
    }
}
