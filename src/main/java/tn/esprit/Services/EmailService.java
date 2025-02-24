package tn.esprit.Services;

import tn.esprit.Models.Evenement.Evenement;
import tn.esprit.Services.Evenement.CategorieEvService;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;

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
    public static void sendEmailWithTemplate(String recipientEmail, String subject, Evenement event, CategorieEvService ces) {
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
            // Charger le template HTML
            InputStream inputStream = EmailService.class.getClassLoader().getResourceAsStream("Evenement/email.html");
            if (inputStream == null) {
                throw new IOException("Fichier email.html introuvable !");
            }
            String template = new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining("\n"));
            Timestamp timestamp = event.getDate_event(); // Récupérer le Timestamp
            LocalDateTime dateTime = timestamp.toLocalDateTime(); // Convertir en LocalDateTime

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String formattedDate = dateTime.format(formatter);
            String logoUrl = "https://i.postimg.cc/zXFTgVmM/level.png";
            String content = template.replace("{{eventName}}", event.getNom_event())
                    .replace("{{eventDate}}", formattedDate)
                    .replace("{{eventLieu}}", event.getLieu_event())
                    .replace("{{eventCategory}}", ces.getNomCategorieEvent(event.getCategorie_id()))
                    .replace("{{logo}}", logoUrl);

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_SENDER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);

            MimeMultipart multipart = new MimeMultipart("related");

            // Partie HTML
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(content, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            message.setContent(multipart);
            Transport.send(message);
        } catch (MessagingException | IOException e) {
            e.getMessage();
        }
    }
}
