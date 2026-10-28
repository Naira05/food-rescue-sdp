package MVC.user;

import java.util.List;

public abstract class User {
    private int userID;
    private String name;
    private String email;
    private String phone;
    private String passwordHash;
    private String role;
    private boolean loggedIn;

    public User(int userID , String name, String email , String passwordHash, String role){
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.loggedIn = false;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {return phone; }

    public void setPhone(String phone) {this.phone = phone;}

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    //login and logout
    public boolean login(String inputPassword){
        this.loggedIn = this.passwordHash.equals(inputPassword);
        return this.loggedIn;
    }
    public void logout(){
        loggedIn = false;
    }

    public boolean isLoggedIn() {
        return this.loggedIn;
    }

    //update profile
    public void updateProfile(String newName, String newEmail) {
        if (newName != null && !newName.isBlank()) this.name = newName;
        if (newEmail != null && !newEmail.isBlank()) this.email = newEmail;
    }

    //abstract methods
    public abstract List<String> getMenuOptions();
    public abstract String getRole();

}
