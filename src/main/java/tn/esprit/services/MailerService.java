package tn.esprit.services;

import javax.mail.*;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

public class MailerService {
    private static final Logger LOGGER = Logger.getLogger(MailerService.class.getName());

    public static void sendMail(String toEmail, String subject, String body) {
        final String fromEmail = "appReservation@gmail.com";
        final String password = "royi zgox tpya nrko";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        try {
            Authenticator auth = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(fromEmail, password);
                }
            };

            Session session = Session.getInstance(props, auth);
            session.setDebug(true); // Enable debug mode

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromEmail));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            msg.setSubject(subject);
            msg.setText(body);

            LOGGER.info("Attempting to send email to: " + toEmail);
            Transport.send(msg);
            LOGGER.info("Email sent successfully to: " + toEmail);
            System.out.println("Email sent successfully to: " + toEmail);
        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "Failed to send email: " + e.getMessage(), e);
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
