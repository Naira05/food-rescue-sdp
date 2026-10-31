package StrategyDP;

import MVC.database.DataAccessException;
import MVC.database.NotificationRepository;

public class APPStrategy implements CommunicationStrategy{

    private final NotificationRepository repo = new NotificationRepository();
    @Override
    public SendResult sendMessage(String recipient, String message) {
        try {
            repo.insert(recipient, message);
            return new SendResult(true, "Notification saved for " + recipient);
        } catch (DataAccessException e) {
            return new SendResult(false, "Could not save notification: " + e.getMessage());
        }}
}
