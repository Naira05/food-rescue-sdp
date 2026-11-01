
package FactoryDP.Creator;

import FactoryDP.Product.CashDonation;
import FactoryDP.Product.DonationProcessor;
import MVC.donation.DonationModel;

public class CashCreator extends DonationCreation {

    public CashCreator(DonationModel donationModel) {
        super(donationModel);
    }

    @Override
    protected DonationProcessor createDonation(DonationRequest request) {
        return new CashDonation(
                donationModel,
                request.getDonorID(),
                request.getQuantity(),
                request.getUnit(),
                request.getDescription()
        );
    }
}
