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
    private static final String EMAIL_SENDER = "levelopcorporation@gmail.com"; // Remplace avec ton email
    private static final String EMAIL_PASSWORD = "uwrk lpba zikl xlcq"; // ⚠️ Ne pas exposer publiquement
    private static String generatedOtp;

    public static String getGeneratedOtp() {
        return generatedOtp; // Method to retrieve the OTP
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
    else if (emailType.equals("custom")) {
        return "<html>" +
                "<body style='background-color: #1B1B1B; color: #ffffff; font-family: Arial, sans-serif; text-align: center; padding: 20px;'>" +
                "<div style='max-width: 500px; margin: auto; background-color: #2A2A2A; padding: 20px; border-radius: 10px;'>" +
                "<img src='" + logoUrl + "' alt='Logo LevelOP' style='max-width: 150px; margin-bottom: 10px;'>" +
                "<h2 style='color: #ffffff;'>Notification</h2>" +
                "<p style='font-size: 16px; white-space: pre-wrap;'>" + additionalInfo + "</p>" +
                "<hr style='border: 1px solid #444;'>" +
                "<p style='font-size: 12px; color: #777;'>Cordialement,<br> L'équipe LevelOP</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
        return "";
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
    public static String generateActivationKey() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder key = new StringBuilder();
        Random random = new Random();
        // Generate 5 groups of 5 characters separated by hyphens
        for (int group = 0; group < 5; group++) {
            for (int i = 0; i < 5; i++) {
                key.append(chars.charAt(random.nextInt(chars.length())));
            }
            if (group < 4) key.append('-');
        }
        return key.toString();
    }

    public static void sendPurchaseConfirmationEmail(String recipientEmail, String username, String productName, String platform) {
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
            InputStream inputStream = EmailService.class.getClassLoader().getResourceAsStream("Produit/emailConf.html");
            if (inputStream == null) {
                throw new IOException("Fichier email.html introuvable !");
            }
            String template = new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining("\n"));

            // Generate activation key
            String activationKey = generateActivationKey();
            // Generate a simple order number
            String orderNumber = "ORD-" + System.currentTimeMillis(); // Or use currentCommande.getId() if available

            String logoUrl = "https://i.postimg.cc/zXFTgVmM/level.png";
            String content = template
                    .replace("{{logo}}", logoUrl)
                    .replace("{{username}}", username)
                    .replace("{{productName}}", productName)
                    .replace("{{platform}}", platform)
                    .replace("{{activationKey}}", activationKey)
                    .replace("{{orderNumber}}", orderNumber);

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_SENDER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Merci pour votre achat sur LevelOP"); // Already in French

            MimeMultipart multipart = new MimeMultipart("related");
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(content, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            message.setContent(multipart);
            Transport.send(message);
            System.out.println("Email de confirmation d'achat envoyé avec succès avec la clé : " + activationKey);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }
}
