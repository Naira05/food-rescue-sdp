
package FactoryDP.Creator;

import FactoryDP.Product.DonationProcessor;
import FactoryDP.Product.FoodDonation;
import MVC.donation.DonationModel;

public class FoodCreator extends DonationCreation {

    public FoodCreator(DonationModel donationModel) {
        super(donationModel);
    }

    @Override
    protected DonationProcessor createDonation(DonationRequest request) {
        return new FoodDonation(
                donationModel,
                request.getDonorID(),
                request.getQuantity(),
                request.getUnit(),
                request.getDescription(),
                request.getExpiryDate()
        );
    }
}
