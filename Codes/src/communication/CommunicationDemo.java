package communication;

public class CommunicationDemo {
    public static void main(String[] args) {
        CommunicationManager manager = new CommunicationManager(new EmailStrategy());
        manager.sendMessage("donor@mail.com", "Thank you for your donation!");

        manager.setStrategy(new SMSStrategy());
        manager.sendMessage("01012345678", "Your volunteer shift is tomorrow.");

        manager.setStrategy(new SocialMediaStrategy());
        manager.sendMessage("@beneficiary", "New aid distribution event!");
    }
}