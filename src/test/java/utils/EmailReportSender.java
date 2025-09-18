package utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.activation.*;

import java.util.Properties;

public class EmailReportSender {

    public static void sendReport(String toEmail, String reportPath) {
        // SMTP Configuration (example for Gmail)
        String host = "smtp.gmail.com";
        final String username = "yourEmail@gmail.com"; // your email
        final String password = "yourAppPassword";    // ⚠️ App Password (not normal password)

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");

        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };

        try {
            Session session = Session.getInstance(props, auth);

            // Create Message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(toEmail)
            );
            message.setSubject("Automation Test Report");

            // Body
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Hi Team,\n\nPlease find attached the latest automation report.\n\nRegards,\nAutomation Bot");

            // Attachment
            MimeBodyPart attachmentPart = new MimeBodyPart();
            DataSource source = new FileDataSource(reportPath);
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName("TestReport.html");

            // Combine body + attachment
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            // Send mail
            Transport.send(message);

            System.out.println("✅ Report emailed successfully to " + toEmail);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
