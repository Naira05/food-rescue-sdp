package StrategyDP;

public class SocialMediaStrategy implements CommunicationStrategy {

    @Override
    public void sendMessage(String recipient, String message) {
        System.out.println("[SOCIAL MEDIA] Post/DM to: " + recipient);
        System.out.println("Message: " + message);
    }
}
