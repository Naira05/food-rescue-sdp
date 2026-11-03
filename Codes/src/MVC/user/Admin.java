package MVC.user;

import java.util.List;

public class Admin extends User {

    public Admin(int userID, String name, String email, String passwordHash, String role) {
        super(userID, name, email, passwordHash, role);
    }

    public static Admin restore(int userId, String name, String email, String passwordHash) {
        Admin a = new Admin(userId, name, email, passwordHash, "Admin");
        return a;
    }

    // implementation for abstract methods
    @Override
    public String getRole() {
        return "Admin";
    }

    @Override
    public List<String> getMenuOptions() {
        return List.of("List users", "Remove user", "List donation types", "Add donation type",
                "Remove donation type", "List donation records", "Update donation status",
                "Delete donation record", "Generate report", "Rename donation type");
    }
}
