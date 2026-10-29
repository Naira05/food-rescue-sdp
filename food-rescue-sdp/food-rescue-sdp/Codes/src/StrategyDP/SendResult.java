package StrategyDP;

public class SendResult {

    private final boolean success;
    private final String details;

    public SendResult(boolean success, String details) {
        this.success = success;
        this.details = details;
    }

    public boolean isSuccess() { return success; }
    public String getDetails() { return details; }
}
