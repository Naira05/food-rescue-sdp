package StrategyDP;

public class SMSStrategy implements CommunicationStrategy{

    @Override
    public void sendMessage(String recipient, String message) {
        System.out.println("Sending SMS to: " + recipient);
        System.out.println("Message: " + message);
    }
}
