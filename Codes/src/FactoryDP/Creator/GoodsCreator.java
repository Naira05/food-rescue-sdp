
package FactoryDP.Creator;

import FactoryDP.Product.DonationProcessor;
import FactoryDP.Product.GoodsDonation;
import MVC.donation.DonationModel;

public class GoodsCreator extends DonationCreation {

    public GoodsCreator(DonationModel donationModel) {
        super(donationModel);
    }

    @Override
    protected DonationProcessor createDonation(DonationRequest request) {
        return new GoodsDonation(
                donationModel,
                request.getDonorID(),
                request.getQuantity(),
                request.getUnit(),
                request.getDescription()
        );
    }
}
