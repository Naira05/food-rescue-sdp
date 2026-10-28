package MVC.donation;

import ObserverDP.DonationObserver;

import java.util.List;

public class DonationView implements DonationObserver {

    public void showDonationForm() {
        System.out.println("""
                ---- New Donation ----
                Type:        (e.g. Cooked meals, Produce, Packaged)
                Description:
                Quantity:    Unit: (kg / meals / boxes)
                Expiry date: (yyyy-MM-dd)
                ----------------------""");
    }

    public void displayDonations(List<Donation> donations) {
        System.out.println("---- Donations (" + donations.size() + ") ----");
        if (donations.isEmpty()) System.out.println("(none)");
        donations.forEach(d -> System.out.println("  " + d));
    }

    public void showReceipt(Donation d) {
        System.out.println("""
                ======== RECEIPT ========
                Donation ID : %d
                Donor ID    : %d
                Item        : %s (%s)
                Quantity    : %.1f %s
                Date        : %s
                Expires     : %s
                =========================""".formatted(
                d.getDonationID(), d.getDonorID(), d.getDescription(), d.getType(),
                d.getQuantity(), d.getUnit(), d.getDonationDate(), d.getExpiryDate()));
    }

    @Override
    public void update(Donation donation) {
        System.out.println("[View] model changed -> " + donation);
    }
}
