
package ObserverDP;

import StrategyDP.CommunicationManager;
import StrategyDP.EmailStrategy;
import StrategyDP.APPStrategy;
import MVC.database.Database;
import MVC.database.DonationRepository;
import DecoratorDP.BasicDonationOperation;
import MVC.donation.Donation;
import MVC.donation.DonationModel;
import DecoratorDP.DonationOperation;
import MVC.donation.DonationView;
import DecoratorDP.ReceiptDecorator;
import DecoratorDP.TaxDocumentDecorator;
import MVC.user.Donor;

import java.sql.Statement;
import java.time.LocalDate;
import java.util.Optional;

public class NotificationDemo {

    public static void main(String[] args) throws Exception {

        // Open an in-memory SQLite database
        Database.open("jdbc:sqlite::memory:");

        try (Statement st = Database.get().createStatement()) {
            st.execute(
                    "INSERT INTO users(role, name, email, password_hash) "
                            + "VALUES ('DONOR', 'Dina', 'dina@mail.com', 'x')"
            );
        }

        // Create the donation model
        DonationModel model = new DonationModel(
                new DonationRepository()
        );

        // Create the donor
        Donor dina = new Donor(
                1, "Dina", "dina@mail.com", "x", "Donor", "Cairo"
        );

        // Create the view
        DonationView view = new DonationView();

        // Create the donor notification observer
        DonorNotification donorObserver = new DonorNotification(
                new CommunicationManager(new EmailStrategy()),
                id -> id == 1
                        ? Optional.of(dina)
                        : Optional.empty()
        );

        // Create the admin notification observer
        AdminNotification adminObserver = new AdminNotification(
                new CommunicationManager(new APPStrategy()),
                "01012345678"
        );

        // Register the observers
        model.attach(view);
        model.attach(donorObserver);
        model.attach(adminObserver);

        // Create the basic donation operation
        DonationOperation operation =
                new BasicDonationOperation(model);

        // Add the receipt decorator
        operation = new ReceiptDecorator(operation);

        // Add the donation record decorator
        operation = new TaxDocumentDecorator(operation);

        // Save the donation through the decorated operation
        Donation donation = operation.execute(new Donation(
                0,
                1,
                "Produce",
                5,
                "kg",
                LocalDate.now().plusDays(3),
                LocalDate.now(),
                Donation.Status.AVAILABLE,
                "Tomatoes"
        ));

        // Change the donation status
        System.out.println("\n--- status change ---");

        donation.setSt(Donation.Status.CLAIMED);
        model.updateDonation(donation);

        // Detach the view and remove the donation
        System.out.println(
                "\n--- view detached, donation removed ---"
        );

        model.detach(view);
        model.removeDonation(donation.getDonationID());

        // Close the database
        Database.close();
    }
}
