package StrategyDP;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailStrategy implements CommunicationStrategy {

    private static final String SENDER = "sdpproject41@gmail.com";
    private static final String APP_PASSWORD = "iojhhjjotmdzxjmp";

    @Override
    public SendResult sendMessage(String recipient, String message) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER, APP_PASSWORD);
            }
        });

        try {
            MimeMessage mail = new MimeMessage(session);
            mail.setFrom(new InternetAddress(SENDER));
            mail.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            mail.setSubject("Food Rescue Notification");
            mail.setText(message);
            Transport.send(mail);
            return new SendResult(true, "Email sent to " + recipient);
        } catch (MessagingException e) {
            return new SendResult(false, "Failed to send email: " + e.getMessage());
        }
    }
}