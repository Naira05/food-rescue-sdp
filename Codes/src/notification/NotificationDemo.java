package notification;

import communication.CommunicationManager;
import communication.EmailStrategy;
import communication.SMSStrategy;
import database.Database;
import database.DonationRepository;
import donation.BasicDonationOperation;
import donation.Donation;
import donation.DonationModel;
import donation.DonationView;
import user.Donor;

import java.sql.Statement;
import java.time.LocalDate;
import java.util.Optional;


public class NotificationDemo {
    public static void main(String[] args) throws Exception {
        Database.open("jdbc:sqlite::memory:");
        try (Statement st = Database.get().createStatement()) {
            st.execute("INSERT INTO users(role, name, email, password_hash) "
                    + "VALUES ('DONOR', 'Dina', 'dina@mail.com', 'x')");
        }


        DonationModel model = new DonationModel(new DonationRepository());


        Donor dina = new Donor(1, "Dina", "dina@mail.com", "x", "Donor", "Cairo");
        DonationView view = new DonationView();
        DonorNotification donorObserver = new DonorNotification(
                new CommunicationManager(new EmailStrategy()),
                id -> id == 1 ? Optional.of(dina) : Optional.empty());
        AdminNotification adminObserver = new AdminNotification(
                new CommunicationManager(new SMSStrategy()), "01012345678");

        model.attach(view);
        model.attach(donorObserver);
        model.attach(adminObserver);


        Donation donation = new BasicDonationOperation(model).execute(new Donation(
                0, 1, "Produce", 5, "kg", LocalDate.now().plusDays(3),
                LocalDate.now(), Donation.Status.AVAILABLE, "Tomatoes"));


        System.out.println("\n--- status change ---");
        donation.setSt(Donation.Status.CLAIMED);
        model.updateDonation(donation);


        System.out.println("\n--- view detached, donation removed ---");
        model.detach(view);
        model.removeDonation(donation.getDonationID());

        Database.close();
    }
}