package StrategyDP;

public class EmailStrategy implements CommunicationStrategy {

    @Override
    public void sendMessage(String recipient, String message) {
        System.out.println("Sending Email to: " + recipient);
        System.out.println("Message: " + message);
    }
}
