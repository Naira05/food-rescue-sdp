package FactoryDP.Creator;

import java.time.LocalDate;

/**
 The data a donor submits. One request type for every kind of donation,
 so all creators share the same factory-method signature.
 For cash, quantity is the amount and unit is the currency.
 */
public final class DonationRequest {

    private final int donorID;
    private final double quantity;
    private final String unit;
    private final String description;
    private final LocalDate expiryDate; // null when the donation does not expire

    public DonationRequest(int donorID, double quantity, String unit,
                           String description, LocalDate expiryDate) {
        this.donorID = donorID;
        this.quantity = quantity;
        this.unit = unit;
        this.description = description;
        this.expiryDate = expiryDate;
    }

    public static DonationRequest cash(int donorID, double amount, String currency, String description) {
        return new DonationRequest(donorID, amount, currency, description, null);
    }

    public static DonationRequest food(int donorID, double quantity, String unit,
                                       String description, LocalDate expiryDate) {
        return new DonationRequest(donorID, quantity, unit, description, expiryDate);
    }

    public static DonationRequest goods(int donorID, double quantity, String unit, String description) {
        return new DonationRequest(donorID, quantity, unit, description, null);
    }

    public int getDonorID() {
        return donorID;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }
}
