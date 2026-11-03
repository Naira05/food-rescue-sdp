package ObserverDP;
import StrategyDP.SendResult;
import StrategyDP.CommunicationManager;
import MVC.user.User;

import java.util.Optional;
import java.util.function.Function;

public class AdminNotification implements DonationObserver {

    private final CommunicationManager communicationManager;
    private final String adminRecipient;
    private final Function<Integer, Optional<User>> donorLookup;

    public AdminNotification(CommunicationManager communicationManager, String adminRecipient,Function<Integer, Optional<User>> donorLookup) {
        this.communicationManager = communicationManager;
        this.adminRecipient = adminRecipient;
        this.donorLookup = donorLookup;
    }

    @Override
    public void update(DonationEvent event) {
        String donorName = donorLookup.apply(event.donation().getDonorID())
                .map(User::getName)
                .orElse("Unknown donor");

        String message = "[ADMIN] " + event.type() + " by " + donorName  + ": " + event.donation();
        if (event.previousStatus() != null) {
            message += " (was " + event.previousStatus() + ")";
        }
        SendResult result = communicationManager.sendMessage(adminRecipient, message);
        if (!result.isSuccess()) {
            System.err.println("[Notification] " + result.getDetails());
        }
    }
}