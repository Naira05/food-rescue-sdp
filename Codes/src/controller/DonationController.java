
package controller;

import donation.BasicDonationOperation;
import donation.Donation;
import donation.DonationModel;
import donation.DonationOperation;
import donation.ReceiptDecorator;
import donation.TaxDocumentDecorator;

public class DonationController {

    private final DonationOperation donationOperation;

    public DonationController(DonationModel donationModel) {

        DonationOperation operation =
                new BasicDonationOperation(donationModel);

        operation = new ReceiptDecorator(operation);

        operation = new TaxDocumentDecorator(operation);

        this.donationOperation = operation;
    }

    public Donation saveDonation(Donation donation) {
        return donationOperation.execute(donation);
    }
}
