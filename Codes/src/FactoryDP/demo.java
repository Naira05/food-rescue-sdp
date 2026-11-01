
package FactoryDP;

import FactoryDP.Creator.*;
import MVC.controller.UserController;
import MVC.database.Database;
import MVC.database.DonationRepository;
import MVC.database.UserRepository;
import MVC.donation.Donation;
import MVC.donation.DonationModel;
import MVC.user.User;

import java.time.LocalDate;
import java.util.List;

public class demo {

    public static void main(String[] args) {

        // 1. Open database
        Database.open("jdbc:sqlite:donations.db");

        try {
            // 2. Find the demo donor, or register it on the first run
            UserController users = new UserController(new UserRepository());
            String email = "ahmed.donor@example.com";
            User donor = users.findByEmail(email).orElseGet(() ->
                    users.registerDonor("Ahmed Mohamed", email, "secret123", "Cairo, Egypt"));
            System.out.println("Donor: " + donor.getName() + " (ID " + donor.getUserID() + ")");

            // 3. Model that every creator saves through
            DonationModel model = new DonationModel(new DonationRepository());

            // 4. Same call (donate) for every creator; each subclass decides
            //    which DonationProcessor the factory method returns
            int id = donor.getUserID();
            List<DonationCreation> creators = List.of(
                    new CashCreator(model),
                    new FoodCreator(model),
                    new GoodsCreator(model)
            );
            List<DonationRequest> requests = List.of(
                    DonationRequest.cash(id, 500.00, "EGP", "Cash donation for charity"),
                    DonationRequest.food(id, 30, "meals", "Chicken & rice", LocalDate.now().plusDays(2)),
                    DonationRequest.goods(id, 10, "pcs", "Winter blankets")
            );

            for (int i = 0; i < creators.size(); i++) {
                Donation saved = creators.get(i).donate(requests.get(i));
                System.out.println("  -> " + saved);
            }

            // 5. Invalid input is rejected before anything is saved
            try {
                new FoodCreator(model).donate(DonationRequest.food(
                        id, 5, "kg", "Old bread", LocalDate.now().minusDays(1)));
            } catch (IllegalArgumentException e) {
                System.out.println("Rejected: " + e.getMessage());
            }
        } finally {
            Database.close();
        }
    }
}
