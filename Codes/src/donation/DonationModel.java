package donation;


import database.DonationRepository;
import donation.Donation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DonationModel {
    private final DonationRepository repository;
    private final List<Donation> donations = new ArrayList<>();
    private final List<DonationObserver> observers = new ArrayList<>();

    public DonationModel(DonationRepository repository) {
        this.repository = repository;
    }

    // observer things
    public void attach(DonationObserver o) {
        if (!observers.contains(o)) {
            observers.add(o);
        }
    }

    public void detach(DonationObserver o) {
        observers.remove(o);
    }

    public void notifyObservers(Donation d) {
        for (DonationObserver o : observers) {
            o.update(d);
        }
    }

    // donation methods
    public Donation saveDonation(Donation donation) {
        repository.insert(donation);
        notifyObservers(donation);
        return donation;
    }
   public Optional<Donation> findDonation(int id){
        return repository.findById(id);
   }
   public List<Donation> findAll(){
        return repository.findAll();
   }
   public List<Donation> findByDonor(int donorId){
        return repository.findByDonor(donorId);

   }
   public Donation updateDonation(Donation d){
        if(!repository.update(d)){
            throw new IllegalArgumentException("No donation with ID: " + d.getDonationID() + "was found");
        }
        notifyObservers(d);
        return d;
   }
   public boolean removeDonation(int id){
        Optional<Donation> currentDonation = repository.findById(id);
        if(currentDonation.isEmpty()){
            return false;
        }
        repository.delete(id);
        notifyObservers(currentDonation.get());
        return true;
   }
}