
package DecoratorDP;

import MVC.donation.Donation;

public class TaxDocumentDecorator
        extends DonationOperationDecorator {

    public TaxDocumentDecorator(DonationOperation operation) {
        super(operation);
    }

    @Override
    public Donation execute(Donation donation) {

        Donation savedDonation = super.execute(donation);

        generateTaxDocument(savedDonation);

        return savedDonation;
    }

    private void generateTaxDocument(Donation donation) {
        System.out.println("\n===== DONATION DOCUMENT =====");
        System.out.println("Document Type: Donation Record");
        System.out.println("Donation ID: "
                + donation.getDonationID());
        System.out.println("Donor ID: "
                + donation.getDonorID());
        System.out.println("Donation Type: "
                + donation.getType());
        System.out.println("Quantity: "
                + donation.getQuantity() + " "
                + donation.getUnit());
        System.out.println("Donation Date: "
                + donation.getDonationDate());
        System.out.println("Status: "
                + donation.getSt());
        System.out.println("=============================\n");
    }
}
