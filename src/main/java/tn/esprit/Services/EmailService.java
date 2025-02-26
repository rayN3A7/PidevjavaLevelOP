package tn.esprit.Services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Random;

public class EmailService {
    private static final String EMAIL_SENDER = "hsouna.sellami07@gmail.com";
    private static final String EMAIL_PASSWORD = "tvjh oytw mmjs yfap";
    private static String generatedOtp;

    public static String getGeneratedOtp() {
        return generatedOtp;
    }

    public static String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        generatedOtp = String.valueOf(otp);
        return generatedOtp;
    }

    public static void sendEmail(String recipientEmail, String subject, String emailType, String additionalInfo) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_SENDER, EMAIL_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_SENDER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);

            String htmlContent = getEmailTemplate(emailType, additionalInfo);

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("E-mail envoyé avec succès !");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private static String getEmailTemplate(String emailType, String additionalInfo) {
        String logoUrl = "https://i.postimg.cc/nhVJRFZ5/logo.png";

        if (emailType.equals("otp")) {
            return "<html>" +
                    "<body style='background-color: #1B1B1B; color: #ffffff; font-family: Arial, sans-serif; text-align: center; padding: 20px;'>" +
                    "<div style='max-width: 500px; margin: auto; background-color: #2A2A2A; padding: 20px; border-radius: 10px;'>" +
                    "<img src='" + logoUrl + "' alt='Logo LevelOP' style='max-width: 150px; margin-bottom: 10px;'>" +
                    "<h2 style='color: #ffffff;'>Bonjour,</h2>" +
                    "<p style='font-size: 16px;'>Utilisez le code ci-dessous pour vérifier votre identité :</p>" +
                    "<div style='background-color: #000000; padding: 10px; border-radius: 5px;'>" +
                    "<h1 style='color: #4CAF50; font-size: 32px;'>" + additionalInfo + "</h1>" +
                    "</div>" +
                    "<p style='font-size: 14px; color: #aaaaaa;'>Si vous n'avez pas demandé ce code, veuillez ignorer cet e-mail.</p>" +
                    "<hr style='border: 1px solid #444;'>" +
                    "<p style='font-size: 12px; color: #777;'>Cordialement, <br> L'équipe LevelOP</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";
        } else if (emailType.equals("coach_accepted")) {
            return "<html>" +
                    "<body style='background-color: #1B1B1B; color: #ffffff; font-family: Arial, sans-serif; text-align: center; padding: 20px;'>" +
                    "<div style='max-width: 500px; margin: auto; background-color: #2A2A2A; padding: 20px; border-radius: 10px;'>" +
                    "<img src='" + logoUrl + "' alt='Logo LevelOP' style='max-width: 150px; margin-bottom: 10px;'>" +
                    "<h2 style='color: #4CAF50;'>Félicitations !</h2>" +
                    "<p style='font-size: 16px;'>Votre demande pour devenir coach sur <strong>LevelOP</strong> a été acceptée !</p>" +
                    "<p style='font-size: 14px; color: #aaaaaa;'>Nous sommes ravis de vous accueillir dans notre équipe.</p>" +
                    "<hr style='border: 1px solid #444;'>" +
                    "<p style='font-size: 12px; color: #777;'>À bientôt,<br> L'équipe LevelOP</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";
        } else if (emailType.equals("coach_refused")) {
            return "<html>" +
                    "<body style='background-color: #1B1B1B; color: #ffffff; font-family: Arial, sans-serif; text-align: center; padding: 20px;'>" +
                    "<div style='max-width: 500px; margin: auto; background-color: #2A2A2A; padding: 20px; border-radius: 10px;'>" +
                    "<img src='" + logoUrl + "' alt='Logo LevelOP' style='max-width: 150px; margin-bottom: 10px;'>" +
                    "<h2 style='color: #FF3B3B;'>Votre demande a été refusée</h2>" +
                    "<p style='font-size: 16px;'>Nous sommes désolés, mais votre demande pour devenir coach sur <strong>LevelOP</strong> n'a pas été acceptée.</p>" +
                    "<p style='font-size: 14px;'>Raison : <strong>" + additionalInfo + "</strong></p>" +
                    "<p style='font-size: 14px; color: #aaaaaa;'>N'hésitez pas à nous contacter pour plus d'informations.</p>" +
                    "<hr style='border: 1px solid #444;'>" +
                    "<p style='font-size: 12px; color: #777;'>Cordialement,<br> L'équipe LevelOP</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";
        }
        return "";
    }
}
