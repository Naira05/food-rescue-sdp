package ObserverDP;


import MVC.donation.Donation;

public abstract class DonationEventObserver implements DonationObserver {

    @Override
    public final void update(Donation donation) {
        // intentionally empty
    }

    @Override
    public void showDonationForm() {
        // intentionally empty
    }

    @Override
    public abstract void update(DonationEvent event);
}