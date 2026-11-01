
package FactoryDP.Product;

import MVC.donation.Donation;
import MVC.donation.DonationModel;

import java.time.LocalDate;

public class FoodDonation implements DonationProcessor {

    private final DonationModel donationModel;
    private final int donorID;
    private final double quantity;
    private final String unit;
    private final String description;
    private final LocalDate expiryDate;

    public FoodDonation(
            DonationModel donationModel,
            int donorID,
            double quantity,
            String unit,
            String description,
            LocalDate expiryDate
    ) {
        if (donationModel == null) {
            throw new IllegalArgumentException(
                    "DonationModel cannot be null"
            );
        }

        if (donorID <= 0) {
            throw new IllegalArgumentException(
                    "Donor ID must be positive"
            );
        }

        if (!Double.isFinite(quantity) || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be positive and finite"
            );
        }

        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException(
                    "Unit is required (kg, meals, boxes...)"
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Description cannot be empty"
            );
        }

        // food that has already expired cannot be rescued
        if (expiryDate == null || expiryDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Expiry date must be today or later"
            );
        }

        this.donationModel = donationModel;
        this.donorID = donorID;
        this.quantity = quantity;
        this.unit = unit.trim();
        this.description = description.trim();
        this.expiryDate = expiryDate;
    }

    @Override
    public Donation process() {
        Donation donation = new Donation(
                0,
                donorID,
                "FOOD",
                quantity,
                unit,
                expiryDate,
                LocalDate.now(),
                Donation.Status.AVAILABLE,
                description
        );

        Donation saved = donationModel.saveDonation(donation);

        System.out.println(
                "Food donation saved successfully. ID: "
                        + saved.getDonationID()
                        + ", Quantity: "
                        + saved.getQuantity()
                        + " "
                        + saved.getUnit()
                        + ", Expires: "
                        + saved.getExpiryDate()
        );
        return saved;
    }
}
