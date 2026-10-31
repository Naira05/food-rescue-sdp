package MVC.donation;

import java.time.LocalDate;
import java.util.List;

public class Donation {

    public static final class Status {
        public static final Status AVAILABLE = new Status("AVAILABLE");
        public static final Status CLAIMED   = new Status("CLAIMED");
        public static final Status PICKED_UP = new Status("PICKED_UP");
        public static final Status EXPIRED   = new Status("EXPIRED");

        private static final List<Status> ALL = List.of(AVAILABLE, CLAIMED, PICKED_UP, EXPIRED);

        private final String name;

        private Status(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }

        public static List<Status> values() {
            return ALL;
        }


        public static Status of(String name) {
            for (Status s : ALL) {
                if (s.name.equals(name)) {
                    return s;
                }
            }
            throw new IllegalArgumentException("Unknown status: " + name);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private int donationID;
    private final int donorID;
    private final String type;
    public String description;
    private double quantity;
    private final String unit;
    private final LocalDate expiryDate;
    private final LocalDate donationDate;
    private Status st = Status.AVAILABLE;

    public Donation(int donationID, int donorID, String type, double quantity, String unit,
                    LocalDate expiryDate, LocalDate donationDate, Status st, String description) {
        this.donationID = donationID;
        this.donorID = donorID;
        this.type = type;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
        this.description = description;
        this.donationDate = donationDate;
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

    public static Donation restore(int donationId, int donorId, String type, double quantity, String unit,
                                   LocalDate expiryDate, String description, LocalDate donationDate, Status st) {
        return new Donation(donationId, donorId, type, quantity, unit, expiryDate, donationDate, st, description);
    }

    @Override
    public String toString() {
        return String.format("Donation#%d [%s] %s - %.1f %s, expires %s, %s (donor %d)",
                donationID, type, description, quantity, unit, expiryDate, st, donorID);
    }
}