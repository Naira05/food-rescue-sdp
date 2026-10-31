
package DecoratorDP;

import MVC.donation.Donation;

public class ReceiptDecorator
        extends DonationOperationDecorator {

    public ReceiptDecorator(DonationOperation operation) {
        super(operation);
    }

    @Override
    public Donation execute(Donation donation) {

        Donation savedDonation = super.execute(donation);

        generateReceipt(savedDonation);

        return savedDonation;
    }

    private void generateReceipt(Donation donation) {
        System.out.println("\n===== DONATION RECEIPT =====");
        System.out.println("Donation ID: "
                + donation.getDonationID());
        System.out.println("Donor ID: "
                + donation.getDonorID());
        System.out.println("Type: "
                + donation.getType());
        System.out.println("Description: "
                + donation.getDescription());
        System.out.println("Quantity: "
                + donation.getQuantity() + " "
                + donation.getUnit());
        System.out.println("Donation Date: "
                + donation.getDonationDate());
        System.out.println("Status: "
                + donation.getSt());
        System.out.println("============================\n");
    }
}
