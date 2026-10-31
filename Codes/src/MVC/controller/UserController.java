package MVC.controller;

import MVC.database.UserRepository;
import MVC.user.Admin;
import MVC.user.Beneficiary;
import MVC.user.Donor;
import MVC.user.PasswordHasher;
import MVC.user.User;
import MVC.user.Volunteer;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class UserController {
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserRepository users;

    public UserController(UserRepository users) {
        this.users = users;
    }

    public Donor registerDonor(String name, String email, String password, String address) {
        validate(name, email, password);
        Donor donor = new Donor(0, name.trim(), email.trim(), PasswordHasher.hash(password), "Donor", address);
        return (Donor) users.insert(donor);
    }

    public Volunteer registerVolunteer(String name, String email, String password,
                                       List<String> skills, String availability) {
        validate(name, email, password);
        Volunteer volunteer = new Volunteer(0, name.trim(), email.trim(), PasswordHasher.hash(password),
                "Volunteer", availability, skills);
        return (Volunteer) users.insert(volunteer);
    }

    public Beneficiary registerBeneficiary(String name, String email, String password,
                                           String needs) {
        validate(name, email, password);
        Beneficiary beneficiary = new Beneficiary(0, name.trim(), email.trim(),
                PasswordHasher.hash(password), "Beneficiary", needs);
        return (Beneficiary) users.insert(beneficiary);
    }

    public Admin createAdmin(String name, String email, String password) {
        validate(name, email, password);
        Admin admin = new Admin(0, name.trim(), email.trim(), PasswordHasher.hash(password), "Admin");
        return (Admin) users.insert(admin);
    }

    public Optional<User> login(String email, String password) {
        if (email == null || password == null) {
            return Optional.empty();
        }
        return findByEmail(email).filter(u -> u.login(password));
    }

    public void logout(User user) {
        if (user != null) {
            user.logout();
        }
    }

    public Optional<User> findByEmail(String email) {
        return email == null ? Optional.empty() : users.findByEmail(email.trim());
    }

    public Optional<User> findById(int userId) {
        return users.findById(userId);
    }

    public List<User> getAllUsers() {
        return users.findAll();
    }
    public boolean removeUser(int userId) {
        return users.delete(userId);
    }

    public void updateProfile(User user, String newName, String newEmail) {
        if (newEmail != null && !newEmail.isBlank()) {
            checkEmail(newEmail);
            findByEmail(newEmail).filter(u -> u.getUserID() != user.getUserID()).ifPresent(u -> {
                throw new IllegalArgumentException("That e-mail is already registered");
            });
        }
        user.updateProfile(newName, newEmail);
        users.updateProfile(user.getUserID(), user.getName(), user.getEmail());
    }

    public void assignTask(Volunteer volunteer, String task) {
        if (task == null || task.isBlank()) {
            throw new IllegalArgumentException("Task is required");
        }
        users.addAssignment(volunteer.getUserID(), task);
        volunteer.assign(task);
    }

    public Beneficiary.FoodRequest requestFood(Beneficiary beneficiary, String item, double quantity) {
        Beneficiary.FoodRequest request = beneficiary.requestFood(item, quantity); // validates
        users.addFoodRequest(beneficiary.getUserID(), request);
        return request;
    }

    private void validate(String name, String email, String password) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        checkEmail(email);
        if (users.findByEmail(email.trim()).isPresent()) {
            throw new IllegalArgumentException("That e-mail is already registered");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
    }

    private void checkEmail(String email) {
        if (email == null || !EMAIL.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Invalid e-mail address");
        }
    }

}
