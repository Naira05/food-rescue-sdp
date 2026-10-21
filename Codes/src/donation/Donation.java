package donation;

import java.time.LocalDate;

public class Donation {
    public enum Status{AVAILABLE, CLAIMED,PICKED_UP, EXPIRED};

    private int donationID;
    private final int donorID;
    private final String type;
    public String description;
    private double quantity;
    private final String unit;
    private final LocalDate expiryDate;
    private final LocalDate donationDate;
    Status st = Status.AVAILABLE;

    public Donation(int donationID, int donorID, String type, double quantity, String unit, LocalDate expiryDate, LocalDate donationDate, Status st, String description) {
        this.donationID = donationID;
        this.donorID = donorID;
        this.type = type;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
        this.description = description;
        this.donationDate = LocalDate.now();
        this.st = st;
    }

    public int getDonationID() {
        return donationID;
    }

    public int getDonorID() {
        return donorID;
    }

    public String getType() {
        return type;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public LocalDate getDonationDate() {
        return donationDate;
    }

    public Status getSt() {
        return st;
    }

    public String getDescription() {
        return description;
    }

    public void setDonationID(int donationID) {
        this.donationID = donationID;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setSt(Status st) {
        this.st = st;
    }
    public static Donation restore(int donationId, int donorId, String type, double quantity, String unit, LocalDate expiryDate, String description, LocalDate donationDate, Status st) {
        return new Donation(donationId, donorId, type, quantity, unit, expiryDate, donationDate, st, description);
    }
    @Override
    public String toString() {
        return String.format("Donation#%d [%s] %s - %.1f %s, expires %s, %s (donor %d)",
                donationID, type, description, quantity, unit, expiryDate, st, donorID);
    }
}
