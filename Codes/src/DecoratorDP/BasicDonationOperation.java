
package DecoratorDP;

import MVC.donation.Donation;
import MVC.donation.DonationModel;

public class BasicDonationOperation implements DonationOperation {

    private final DonationModel donationModel;

    public BasicDonationOperation(DonationModel donationModel) {
        this.donationModel = donationModel;
    }

    @Override
    public Donation execute(Donation donation) {
        return donationModel.saveDonation(donation);
    }
}
