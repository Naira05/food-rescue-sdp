package ObserverDP;

import StrategyDP.CommunicationManager;

public class AdminNotification implements DonationObserver {

    private final CommunicationManager communicationManager;
    private final String adminRecipient;

    public AdminNotification(CommunicationManager communicationManager, String adminRecipient) {
        this.communicationManager = communicationManager;
        this.adminRecipient = adminRecipient;
    }

    @Override
    public void update(DonationEvent event) {
        String message = "[ADMIN] " + event.type() + ": " + event.donation();
        if (event.previousStatus() != null) {
            message += " (was " + event.previousStatus() + ")";
        }
        communicationManager.sendMessage(adminRecipient, message);
    }
}