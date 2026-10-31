
package MVC.controller;

import DecoratorDP.BasicDonationOperation;
import MVC.donation.Donation;
import MVC.donation.DonationModel;
import DecoratorDP.DonationOperation;
import DecoratorDP.ReceiptDecorator;
import DecoratorDP.TaxDocumentDecorator;

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
