
package FactoryDP.Product;

import MVC.donation.Donation;
import MVC.donation.DonationModel;

import java.time.LocalDate;

public class CashDonation implements DonationProcessor {

    private final DonationModel donationModel;
    private final int donorID;
    private final double amount;
    private final String currency;
    private final String description;

    public CashDonation(
            DonationModel donationModel,
            int donorID,
            double amount,
            String currency,
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

        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be positive and finite"
            );
        }

        if (currency == null || !currency.trim().matches("[A-Za-z]{3}")) {
            throw new IllegalArgumentException(
                    "Currency must be a 3-letter code (e.g. EGP, USD)"
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "Description cannot be empty"
            );
        }

        this.donationModel = donationModel;
        this.donorID = donorID;
        this.amount = amount;
        this.currency = currency.trim().toUpperCase();
        this.description = description.trim();
    }

    @Override
    public Donation process() {
        Donation donation = new Donation(
                0,
                donorID,
                "CASH",
                amount,
                currency,
                NO_EXPIRY,
                LocalDate.now(),
                Donation.Status.AVAILABLE,
                description
        );

        Donation saved = donationModel.saveDonation(donation);

        System.out.println(
                "Cash donation saved successfully. ID: "
                        + saved.getDonationID()
                        + ", Amount: "
                        + saved.getQuantity()
                        + " "
                        + saved.getUnit()
        );
        return saved;
    }
}
