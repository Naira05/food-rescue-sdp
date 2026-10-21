package communication;

public class CommunicationManager {

    private CommunicationStrategy strategy;

    public CommunicationManager(CommunicationStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(CommunicationStrategy strategy) {
        this.strategy = strategy;
    }

    public void sendMessage(String recipient, String message) {
        strategy.sendMessage(recipient, message);
    }

}
