//NOTE: i didnt write this demo

import FactoryDP.DonationFactory;
import FactoryDP.DonationProcessor;
import MVC.controller.AdminPanel;
import MVC.controller.DonationController;
import MVC.controller.UserController;
import MVC.database.Database;
import MVC.database.DonationRepository;
import MVC.database.UserRepository;
import MVC.donation.Donation;
import MVC.donation.DonationModel;
import MVC.donation.DonationView;
import MVC.user.Admin;
import MVC.user.Beneficiary;
import MVC.user.Donor;
import MVC.user.User;
import MVC.user.Volunteer;
import MVC.view.AdminControlPanel;
import MVC.view.ConsoleIO;
import MVC.view.MainMenu;
import MVC.view.UserControlPanel;
import ObserverDP.AdminNotification;
import ObserverDP.DonorNotification;
import StrategyDP.APPStrategy;
import StrategyDP.CommunicationManager;
import StrategyDP.CommunicationStrategy;
import StrategyDP.SendResult;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

/**
 * Tests the whole project end to end.
 *
 *   java Main               scripted demo + self-checks (uses a temporary database file)
 *   java Main interactive   real console app (sign up / log in), data kept in foodrescue.db
 *
 * Needs the SQLite JDBC driver (sqlite-jdbc jar) on the classpath, like your other demos.
 * Notifications use the in-app strategy, so no real e-mail is sent. To send real e-mails,
 * replace ConsoleStrategy in Wiring with new StrategyDP.EmailStrategy().
 */
public class Main {
    private static final String ADMIN_EMAIL = "admin@foodrescue.org";
    private static final String ADMIN_PASSWORD = "admin123";

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equalsIgnoreCase("interactive")) {
            runInteractive();
        } else {
            runDemo();
        }
    }

    // =====================================================================================
    //  Wiring: builds the model, controllers and observers (shared by both modes)
    // =====================================================================================
    private static final class Wiring {
        final DonationModel model;
        final UserController users;
        final DonationController donations;
        final CommunicationManager comms;

        Wiring() {
            model = new DonationModel(new DonationRepository());
            users = new UserController(new UserRepository());
            donations = new DonationController(model, new DonationView()); // view observes the model
            comms = new CommunicationManager(new ConsoleStrategy());       // Strategy pattern

            // Observer pattern: notified on every donation change
            model.attach(new DonorNotification(comms, users::findById));
            model.attach(new AdminNotification(comms, ADMIN_EMAIL));
        }
    }

    /** In-app delivery that also prints the message, so the demo shows who was notified. */
    private static final class ConsoleStrategy implements CommunicationStrategy {
        @Override
        public SendResult sendMessage(String recipient, String message) {
            System.out.println("[notify -> " + recipient + "] " + message);
            return new APPStrategy().sendMessage(recipient, message);
        }
    }

    // =====================================================================================
    //  Interactive mode
    // =====================================================================================
    private static void runInteractive() {
        Database.open(System.getProperty("db.url", "jdbc:sqlite:foodrescue.db"));
        try {
            Wiring w = new Wiring();
            if (w.users.findByEmail(ADMIN_EMAIL).isEmpty()) {
                w.users.createAdmin("System Admin", ADMIN_EMAIL, null, ADMIN_PASSWORD);
                System.out.println("First run: admin account created (" + ADMIN_EMAIL + " / " + ADMIN_PASSWORD + ")");
            }
            new MainMenu(w.users, w.donations, ConsoleIO.keyboard()).run();
        } finally {
            Database.close();
        }
        System.out.println("Goodbye.");
    }

    // =====================================================================================
    //  Scripted demo with self-checks
    // =====================================================================================
    private static void runDemo() throws Exception {
        File dbFile = File.createTempFile("foodrescue-demo", ".db");
        dbFile.deleteOnExit();
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        // ---- 1. set-up ---------------------------------------------------------------
        section("1. Set-up: database, MVC wiring, observers");
        Database.open(url);
        Wiring w = new Wiring();
        Admin seededAdmin = w.users.createAdmin("System Admin", ADMIN_EMAIL, "01000000000", ADMIN_PASSWORD);
        check("admin created with a database id", seededAdmin.getUserID() > 0);
        check("donation types seeded", w.donations.getDonationTypes().size() == 3);

        // ---- 2. sign-up ----------------------------------------------------------------
        section("2. Sign-up and validation");
        Donor donorSaved = w.users.registerDonor("Green Bistro", "bistro@example.com", "01011111111",
                "secret1", "12 Nile St, Cairo");
        Volunteer volunteerSaved = w.users.registerVolunteer("Omar", "omar@example.com", "01033333333",
                "secret2", List.of("driving", "lifting"), "Weekends");
        Beneficiary beneficiarySaved = w.users.registerBeneficiary("Hope Shelter", "hope@example.org", "01044444444",
                "secret3", "Hot meals for 40 people");
        check("donor saved with the right name/e-mail/phone",
                donorSaved.getName().equals("Green Bistro")
                        && donorSaved.getEmail().equals("bistro@example.com")
                        && "01011111111".equals(donorSaved.getPhone())
                        && "12 Nile St, Cairo".equals(donorSaved.getAddress()));
        check("volunteer saved with skills and availability",
                volunteerSaved.getSkills().equals(List.of("driving", "lifting"))
                        && "Weekends".equals(volunteerSaved.getAvailability()));
        check("beneficiary saved with needs", "Hot meals for 40 people".equals(beneficiarySaved.getNeeds()));
        check("password is stored hashed", !donorSaved.getPasswordHash().equals("secret1"));

        expectError("duplicate e-mail (different case) is rejected",
                () -> w.users.registerDonor("Dup", "BISTRO@example.com", null, "secret1", "x"),
                "already registered");
        expectError("short password is rejected",
                () -> w.users.registerDonor("Weak", "weak@example.com", null, "123", "x"),
                "at least 6");
        expectError("invalid e-mail is rejected",
                () -> w.users.registerDonor("Bad", "not-an-email", null, "secret1", "x"),
                "Invalid e-mail");
        expectError("invalid phone is rejected",
                () -> w.users.registerDonor("Bad", "bad@example.com", "12ab", "secret1", "x"),
                "Invalid phone");

        // ---- 3. login --------------------------------------------------------------------
        section("3. Login");
        check("wrong password fails", w.users.login("bistro@example.com", "nope").isEmpty());
        check("unknown e-mail fails", w.users.login("ghost@example.com", "secret1").isEmpty());
        check("e-mail login is case-insensitive", w.users.login("BISTRO@example.com", "secret1").isPresent());
        Donor donor = (Donor) w.users.login("bistro@example.com", "secret1").orElseThrow();
        Volunteer volunteer = (Volunteer) w.users.login("omar@example.com", "secret2").orElseThrow();
        Beneficiary beneficiary = (Beneficiary) w.users.login("hope@example.org", "secret3").orElseThrow();
        Admin admin = (Admin) w.users.login(ADMIN_EMAIL, ADMIN_PASSWORD).orElseThrow();
        check("all four roles log in as the right class", donor.isLoggedIn() && volunteer.isLoggedIn()
                && beneficiary.isLoggedIn() && admin.isLoggedIn());

        // ---- 4. Factory and Strategy --------------------------------------------------------
        section("4. Factory pattern and Strategy pattern");
        DonationFactory factory = new DonationFactory();
        for (String type : List.of("cash", "food", "goods")) {
            DonationProcessor processor = factory.createDonation(type);
            processor.process();
        }
        expectError("factory rejects an unknown type", () -> factory.createDonation("gold"), "Invalid donation type");
        SendResult first = w.comms.sendMessage("someone@example.com", "Strategy 1: console + in-app");
        w.comms.setStrategy(new APPStrategy());
        SendResult second = w.comms.sendMessage("volunteer_omar", "Strategy 2: in-app only (swapped at runtime)");
        System.out.println("  " + second.getDetails());
        check("both strategies deliver", first.isSuccess() && second.isSuccess());
        w.comms.setStrategy(new ConsoleStrategy()); // back to the printing strategy for the rest of the demo

        // ---- 5. donor session -------------------------------------------------------------------
        section("5. Donor dashboard (decorators + observers fire on every new donation)");
        LocalDate in2 = LocalDate.now().plusDays(2);
        LocalDate in4 = LocalDate.now().plusDays(4);
        LocalDate yesterday = LocalDate.now().minusDays(1);
        new UserControlPanel(donor, w.donations, w.users, ConsoleIO.scripted(
                "1", "Cooked meals", "Chicken & rice", "30", "meals", in2.toString(),
                "1", "Produce", "Tomatoes", "12.5", "kg", in4.toString(),
                "1", "Gadgets", "Old TV", "1", "pcs", in4.toString(),          // unknown type -> error
                "1", "Produce", "Old bread", "5", "kg", yesterday.toString(),  // expired -> error
                "2",
                "3", "1",
                "3", "999",                                                    // no such donation -> error
                "4", "Green Bistro Cairo", "", "01022222222",
                "0")).run();
        check("two donations saved by the donor", w.model.findByDonor(donor.getUserID()).size() == 2);
        check("new donations start AVAILABLE",
                w.model.findDonation(1).orElseThrow().getSt() == Donation.Status.AVAILABLE);
        User donorInDb = w.users.findById(donor.getUserID()).orElseThrow();
        check("donor profile edit was saved (name and phone)",
                donorInDb.getName().equals("Green Bistro Cairo") && "01022222222".equals(donorInDb.getPhone())
                        && donorInDb.getEmail().equals("bistro@example.com"));
        expectError("a donation cannot be created with quantity 0",
                () -> w.donations.createDonation(donor, "Produce", "x", 0, "kg", in2), "Quantity must be positive");

        // ---- 6. beneficiary session --------------------------------------------------------------------
        section("6. Beneficiary dashboard");
        new UserControlPanel(beneficiary, w.donations, w.users, ConsoleIO.scripted(
                "1",
                "2", "Cooked meals", "20",
                "2", "Rice", "-3",                                             // invalid quantity -> error
                "3",
                "0")).run();
        check("one food request recorded", beneficiary.getRequests().size() == 1);

        // ---- 7. volunteer session ------------------------------------------------------------------------
        section("7. Volunteer dashboard (claiming changes the donation status -> observers)");
        new UserControlPanel(volunteer, w.donations, w.users, ConsoleIO.scripted(
                "1",
                "2", "1",
                "2", "1",                                                      // already claimed -> error
                "3",
                "0")).run();
        check("donation #1 is CLAIMED", w.model.findDonation(1).orElseThrow().getSt() == Donation.Status.CLAIMED);
        check("volunteer has one assignment", volunteer.viewAssignment().size() == 1);
        check("only the unclaimed donation is still available", w.donations.getAvailableDonations().size() == 1);

        // ---- 8. main menu: sign-up, login failure, admin session -------------------------------------------------
        section("8. Main menu (sign up, bad login) and Admin dashboard");
        int nourId = 5; // admin=1, donor=2, volunteer=3, beneficiary=4, then the new sign-up
        new MainMenu(w.users, w.donations, ConsoleIO.scripted(
                "1", "3", "Nour Charity", "nour@example.org", "", "nour123", "Hot meals",
                "2", "nour@example.org", "wrong-password",
                "2", ADMIN_EMAIL, ADMIN_PASSWORD,
                "1",                                                           // list users
                "3",                                                           // list types
                "4", "Bakery",
                "4", "Bakery",                                                 // duplicate
                "5", "Bakery",
                "6",                                                           // list donation records
                "7", "1", "PICKED_UP",                                         // status change -> observers
                "7", "1", "FLYING",                                            // bad status -> error
                "9",                                                           // report
                "2", String.valueOf(donor.getUserID()),                        // donor has donations -> error
                "2", String.valueOf(admin.getUserID()),                        // own account -> error
                "2", String.valueOf(nourId),                                   // no donations -> removed
                "8", "2",                                                      // delete donation #2 -> observers
                "0",                                                           // log out
                "0")).run();                                                   // exit
        check("donation #1 is PICKED_UP", w.model.findDonation(1).orElseThrow().getSt() == Donation.Status.PICKED_UP);
        check("donation #2 was deleted", w.model.findDonation(2).isEmpty());
        check("new sign-up was removed by the admin", w.users.findById(nourId).isEmpty());
        check("donor with donations was not removed", w.users.findById(donor.getUserID()).isPresent());
        check("temporary donation type was added then removed", !w.donations.getDonationTypes().contains("Bakery"));
        check("admin was logged out by the main menu", w.users.findById(admin.getUserID()).isPresent());

        // ---- 9. persistence -------------------------------------------------------------------------------------
        section("9. Persistence: close the database, reopen the same file, read everything back");
        w.users.logout(donor);
        w.users.logout(volunteer);
        w.users.logout(beneficiary);
        w.users.logout(admin);
        Database.close();
        Database.open(url);
        Wiring again = new Wiring();
        Volunteer volunteerAgain = (Volunteer) again.users.login("omar@example.com", "secret2").orElseThrow();
        Beneficiary beneficiaryAgain = (Beneficiary) again.users.login("hope@example.org", "secret3").orElseThrow();
        check("volunteer assignment survived the restart", volunteerAgain.viewAssignment().size() == 1);
        System.out.println("  assignment: " + volunteerAgain.viewAssignment().get(0));
        check("beneficiary food request survived the restart", beneficiaryAgain.getRequests().size() == 1);
        check("donation #1 survived the restart with its status",
                again.model.findDonation(1).orElseThrow().getSt() == Donation.Status.PICKED_UP);
        check("exactly 4 users remain", again.users.getAllUsers().size() == 4);
        check("exactly 1 donation remains", again.model.findAll().size() == 1);
        check("donation types survived (3, none re-seeded)", again.donations.getDonationTypes().size() == 3);

        // ---- 10. access control -----------------------------------------------------------------------------------------
        section("10. Access control after logout");
        expectError("a logged-out user cannot open a dashboard",
                () -> new UserControlPanel(donor, again.donations, again.users, ConsoleIO.scripted("0")).run(),
                "not logged in");
        expectError("a logged-out admin cannot open the admin dashboard",
                () -> new AdminControlPanel(new AdminPanel(admin, again.donations, again.users),
                        ConsoleIO.scripted("0")).run(),
                "not logged in");
        expectError("a logged-out admin cannot call admin actions",
                () -> new AdminPanel(admin, again.donations, again.users).generateReports(),
                "not logged in");

        Database.close();

        // ---- summary -------------------------------------------------------------------------------------------------------------
        System.out.println("\n==========================================");
        System.out.println("  " + passed + " checks passed, " + failed + " failed");
        System.out.println("==========================================");
        if (failed > 0) {
            System.exit(1);
        }
    }

    // =====================================================================================
    //  Tiny test helpers
    // =====================================================================================
    private static void section(String title) {
        System.out.println("\n\n########## " + title + " ##########");
    }

    private static void check(String label, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("  [PASS] " + label);
        } else {
            failed++;
            System.out.println("  [FAIL] " + label);
        }
    }

    private static void expectError(String label, Runnable action, String messagePart) {
        try {
            action.run();
            failed++;
            System.out.println("  [FAIL] " + label + " (no error was thrown)");
        } catch (IllegalArgumentException | IllegalStateException e) {
            check(label + " -> \"" + e.getMessage() + "\"", e.getMessage().contains(messagePart));
        }
    }
}
