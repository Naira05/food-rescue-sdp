
package FactoryDP.Product;

import MVC.donation.Donation;
import MVC.donation.DonationModel;

import java.time.LocalDate;

public class GoodsDonation implements DonationProcessor {

    private final DonationModel donationModel;
    private final int donorID;
    private final double quantity;
    private final String unit;
    private final String description;

    public GoodsDonation(
            DonationModel donationModel,
            int donorID,
            double quantity,
            String unit,
            String description
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

        // goods are counted items (blankets, clothes...), so no fractions
        if (!Double.isFinite(quantity) || quantity <= 0 || quantity != Math.floor(quantity)) {
            throw new IllegalArgumentException(
                    "Quantity must be a positive whole number"
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Description cannot be empty"
            );
        }

        this.donationModel = donationModel;
        this.donorID = donorID;
        this.quantity = quantity;
        this.unit = (unit == null || unit.isBlank()) ? "pcs" : unit.trim();
        this.description = description.trim();
    }

    @Override
    public Donation process() {
        Donation donation = new Donation(
                0,
                donorID,
                "GOODS",
                quantity,
                unit,
                NO_EXPIRY,
                LocalDate.now(),
                Donation.Status.AVAILABLE,
                description
        );

        Donation saved = donationModel.saveDonation(donation);

        System.out.println(
                "Goods donation saved successfully. ID: "
                        + saved.getDonationID()
                        + ", Quantity: "
                        + (long) saved.getQuantity()
                        + " "
                        + saved.getUnit()
        );
        return saved;
    }
}
