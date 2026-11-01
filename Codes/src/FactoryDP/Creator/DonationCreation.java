
package FactoryDP.Creator;

import FactoryDP.Product.DonationProcessor;
import MVC.donation.Donation;
import MVC.donation.DonationModel;


public abstract class DonationCreation {

    protected final DonationModel donationModel;

    protected DonationCreation(DonationModel donationModel) {
        if (donationModel == null) {
            throw new IllegalArgumentException(
                    "DonationModel cannot be null"
            );
        }

        this.donationModel = donationModel;
    }

    public Donation donate(DonationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Donation request cannot be null"
            );
        }

        DonationProcessor processor = createDonation(request);
        return processor.process();
    }

    // the factory method
    protected abstract DonationProcessor createDonation(DonationRequest request);
}
