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

    public static void sendOtpEmail(String recipientEmail, String otp) {
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
            message.setSubject("Your OTP Code for Account Recovery");

            // Replace this URL with the link to your hosted logo
            String logoUrl = "C:\\xampp\\htdocs\\img\\logo.png";

            String htmlContent = "<html>" +
                    "<body style='background-color: #1B1B1B; color: #ffffff; font-family: Arial, sans-serif; text-align: center; padding: 20px;'>" +
                    "<div style='max-width: 500px; margin: auto; background-color: #2A2A2A; padding: 20px; border-radius: 10px;'>" +
                    "<img src='" + logoUrl + "' alt='Your Logo' style='max-width: 150px; margin-bottom: 10px;'>" +
                    "<h2 style='color: #ffffff;'>Hello,</h2>" +
                    "<p style='font-size: 16px;'>Use the code below to verify your identity:</p>" +
                    "<div style='background-color: #000000; padding: 10px; border-radius: 5px;'>" +
                    "<h1 style='color: #4CAF50; font-size: 32px;'>" + otp + "</h1>" +
                    "</div>" +
                    "<p style='font-size: 14px; color: #aaaaaa;'>If you did not request this, please ignore this email.</p>" +
                    "<hr style='border: 1px solid #444;'>" +
                    "<p style='font-size: 12px; color: #777;'>Cheers, <br> LevelOP Team</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("OTP email sent successfully!");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

}
