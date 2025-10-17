package utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.activation.*;

import java.util.Properties;

public class EmailReportSender {

    // configure for your account
    private static final String SMTP_HOST = "smtp.office365.com";
    private static final int SMTP_PORT = 587;
    private static final String FROM_EMAIL = "chetan.patil@dharbor.com";
    private static final String FROM_PASSWORD = "Athanidhi@2025"; // app password or actual if allowed

    public static void sendReport(String toEmail, String reportPath) {
        if (reportPath == null) {
            System.err.println("No report found to send.");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        props.put("mail.smtp.ssl.trust", SMTP_HOST);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Automation Test Report");

            // body
            BodyPart text = new MimeBodyPart();
            text.setText("Hi Team,\n\nPlease find attached the latest automation report.\n\nRegards,\nAutomation Bot");

            // attachment
            MimeBodyPart attachment = new MimeBodyPart();
            DataSource source = new FileDataSource(reportPath);
            attachment.setDataHandler(new DataHandler(source));
            attachment.setFileName(new java.io.File(reportPath).getName());

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(text);
            multipart.addBodyPart(attachment);

            message.setContent(multipart);

            Transport.send(message);
            System.out.println("✅ Report emailed to " + toEmail + " (attached: " + reportPath + ")");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
