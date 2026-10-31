package StrategyDP;

public class CommunicationManager {

    private CommunicationStrategy strategy;

    public CommunicationManager(CommunicationStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(CommunicationStrategy strategy) {
        this.strategy = strategy;
    }

    public SendResult sendMessage(String recipient, String message) {
        return strategy.sendMessage(recipient, message);
    }

}
