package donation;

public interface DonationObserver {
    public void update(Donation donation); //whoever is using this interface please check this
    public void showDonationForm(); // this is used in DonationView
}
