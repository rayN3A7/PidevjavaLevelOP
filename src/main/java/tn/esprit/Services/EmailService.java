package tn.esprit.Services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Random;

public class EmailService {
    private static final String EMAIL_SENDER = "hsouna.sellami07@gmail.com"; // Remplace avec ton email
    private static final String EMAIL_PASSWORD = "tvjh oytw mmjs yfap"; // ⚠️ Ne pas exposer publiquement
    private static String generatedOtp;

    public static String getGeneratedOtp() {
        return generatedOtp; // Method to retrieve the OTP
    }


    public static String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Generate a 6-digit OTP
        generatedOtp = String.valueOf(otp); // Store the OTP
        return generatedOtp;
    }

    public static void sendOtpEmail(String recipientEmail, String otp) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_SENDER, EMAIL_PASSWORD); // Use the constants here
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_SENDER)); // Use the constants here
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Votre code de réinitialisation");
            message.setText("Bonjour,\n\nVotre code de vérification est : " + otp + "\n\nVeuillez l'entrer pour réinitialiser votre mot de passe.");

            Transport.send(message);
            System.out.println("OTP envoyé avec succès !");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
