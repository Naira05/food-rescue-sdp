package user;

import java.util.List;

public class Admin extends User {

    public Admin(int userID, String name, String email, String passwordHash, String role) {
        super(userID, name, email, passwordHash, role);
    }

    //implementation for abstract methods
    @Override
    public String getRole() {
        return "Admin";
    }
    public static Admin restore(int userId, String named, String name, String email, String passwordHash) {
        return new Admin(userId, name, email, passwordHash, "Admin");
    }
    @Override
    public List<String> getMenuOptions() {
        return List.of("List users", "Remove user", "List donation types", "Add donation type",
                "Remove donation type", "List donation records", "Update donation status",
                "Delete donation record", "Generate report");
    }
}

