package ObserverDP;

import MVC.donation.Donation;
import MVC.user.User;
import StrategyDP.CommunicationManager;

import java.util.Optional;
import java.util.function.Function;

public class DonorNotification implements DonationObserver {

    private final CommunicationManager communicationManager;
    private final Function<Integer, Optional<User>> donorLookup;

    public DonorNotification(CommunicationManager communicationManager,
                             Function<Integer, Optional<User>> donorLookup) {
        this.communicationManager = communicationManager;
        this.donorLookup = donorLookup;
    }

    @Override
    public void update(DonationEvent event) {
        String message = buildMessage(event);
        if (message == null) {
            return; // this observer does not care about this kind of event
        }

        Donation donation = event.donation();
        Optional<User> donor = donorLookup.apply(donation.getDonorID());
        if (donor.isEmpty()) {
            return; // nobody to notify
        }

        communicationManager.sendMessage(donor.get().getEmail(), message);
    }

    private String buildMessage(DonationEvent event) {
        Donation d = event.donation();
        DonationEvent.Type type = event.type();

        if (type == DonationEvent.Type.CREATED) {
            return "Thank you! Your donation #" + d.getDonationID()
                    + " (" + d.getDescription() + ") was registered.";
        }
        if (type == DonationEvent.Type.STATUS_CHANGED) {
            return "Your donation #" + d.getDonationID()
                    + " changed from " + event.previousStatus() + " to " + d.getSt() + ".";
        }
        if (type == DonationEvent.Type.REMOVED) {
            return "Your donation #" + d.getDonationID() + " was removed.";
        }
        return null; // UPDATED (and any future type) sends nothing
    }
}