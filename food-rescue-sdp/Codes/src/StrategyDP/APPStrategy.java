package StrategyDP;

public class APPStrategy implements CommunicationStrategy{

    @Override
    public SendResult sendMessage(String recipient, String message) {
        return new SendResult(true, "In-app notification delivered to " + recipient + ": " + message);
    }
}
