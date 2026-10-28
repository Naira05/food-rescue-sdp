package MVC.donation;


import ObserverDP.DonationObserver;
import MVC.database.DonationRepository;
import ObserverDP.DonationEvent;
import ObserverDP.DonationSubject;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DonationModel implements DonationSubject {
    private static final Logger LOG = Logger.getLogger(DonationModel.class.getName());

    private final DonationRepository repository;
    // CopyOnWriteArrayList: an observer may detach itself while being notified
    private final CopyOnWriteArrayList<DonationObserver> observers = new CopyOnWriteArrayList<>();

    public DonationModel(DonationRepository repository) {
        this.repository = repository;
    }

    // observer things
    @Override
    public void attach(DonationObserver o) {
        Objects.requireNonNull(o, "observer");
        observers.addIfAbsent(o);
    }

    @Override
    public void detach(DonationObserver o) {
        observers.remove(o);
    }


    @Override
    public void notifyObservers(DonationEvent event) {
        for (DonationObserver o : observers) {
            try {
                o.update(event);
            } catch (RuntimeException e) {
                LOG.log(Level.WARNING, "Observer " + o.getClass().getSimpleName()
                        + " failed on " + event.type(), e);
            }
        }
    }

    public void notifyObservers(Donation d) {
        notifyObservers(DonationEvent.updated(d));
    }

    // donation methods
    public Donation saveDonation(Donation donation) {
        repository.insert(donation);
        notifyObservers(DonationEvent.created(donation));
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
        // read the stored version first so we can tell whether the status changed
        Optional<Donation> before = repository.findById(d.getDonationID());
        if(!repository.update(d)){
            throw new IllegalArgumentException("No donation with ID: " + d.getDonationID() + "was found");
        }
        if (before.isPresent() && before.get().getSt() != d.getSt()) {
            notifyObservers(DonationEvent.statusChanged(d, before.get().getSt()));
        } else {
            notifyObservers(DonationEvent.updated(d));
        }
        return d;
    }
    public boolean removeDonation(int id){
        Optional<Donation> currentDonation = repository.findById(id);
        if(currentDonation.isEmpty()){
            return false;
        }
        repository.delete(id);
        notifyObservers(DonationEvent.removed(currentDonation.get()));
        return true;
    }
}