package ObserverDP;

import StrategyDP.CommunicationManager;
import MVC.donation.Donation;
import MVC.user.User;

import java.util.Optional;
import java.util.function.Function;

public class DonorNotification extends DonationEventObserver {

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
        return switch (event.type()) {
            case CREATED -> "Thank you! Your donation #" + d.getDonationID()
                    + " (" + d.getDescription() + ") was registered.";
            case STATUS_CHANGED -> "Your donation #" + d.getDonationID()
                    + " changed from " + event.previousStatus() + " to " + d.getSt() + ".";
            case REMOVED -> "Your donation #" + d.getDonationID() + " was removed.";
            case UPDATED -> null;
        };
    }
}