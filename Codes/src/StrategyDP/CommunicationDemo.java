package StrategyDP;

public class CommunicationDemo {
    public static void main(String[] args) {
        CommunicationManager manager = new CommunicationManager(new EmailStrategy());
        show(manager.sendMessage("sdpproject41@gmail.com", "Thank you for your donation!"));

        manager.setStrategy(new APPStrategy());
        show(manager.sendMessage("volunteer_ahmed", "Your volunteer shift is tomorrow."));
    }

    private static void show(SendResult r) {
        System.out.println((r.isSuccess() ? "[OK] " : "[FAIL] ") + r.getDetails());
    }
}