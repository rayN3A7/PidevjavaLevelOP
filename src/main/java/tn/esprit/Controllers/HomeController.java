package tn.esprit.Controllers;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONObject;
import tn.esprit.utils.SessionManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HomeController {
    private final Dotenv dotenv = Dotenv.configure().load();
    boolean isConnected = SessionManager.getInstance().isLoggedIn();

    @FXML
    private void openChatbotDialog() {
        if(isConnected){
        // Créer une nouvelle fenêtre (Stage)
        Stage chatbotStage = new Stage();
        chatbotStage.setTitle("Chatbot LevelOP");
        chatbotStage.initModality(Modality.APPLICATION_MODAL);

        VBox chatbotLayout = new VBox(10);
        chatbotLayout.setStyle("-fx-background-color: #ffffff; -fx-padding: 20; -fx-spacing: 10;");

        TextArea chatArea = new TextArea();
        chatArea.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 10; -fx-font-size: 14px; -fx-text-fill: #2d3436; -fx-wrap-text: true;");
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPrefHeight(300);

        TextField userInput = new TextField();
        userInput.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10; -fx-border-radius: 10; -fx-padding: 10; -fx-font-size: 14px; -fx-text-fill: #2d3436; -fx-prompt-text-fill: #6c757d;");
        userInput.setPromptText("Posez une question...");

        Button sendButton = new Button("Envoyer");
        sendButton.setStyle("-fx-background-color: #00b894; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 10 20; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        sendButton.setOnAction(e -> {
            String userMessage = userInput.getText();
            if (!userMessage.isEmpty()) {
                chatArea.appendText("Vous : " + userMessage + "\n");
                userInput.clear();

                new Thread(() -> {
                    String response = getChatbotResponse(userMessage);
                    Platform.runLater(() -> animateText(chatArea, "Chatbot : " + response + "\n"));
                }).start();
            }
        });

        Button closeButton = new Button("Fermer");
        closeButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 10 20; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        closeButton.setOnAction(e -> chatbotStage.close());

        HBox buttonBox = new HBox(10, sendButton, closeButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        chatbotLayout.getChildren().addAll(chatArea, userInput, buttonBox);
        Scene scene = new Scene(chatbotLayout, 400, 400);
        chatbotStage.setScene(scene);
        chatbotStage.show();}
        else{
            showAlert(Alert.AlertType.INFORMATION, "Information", "Vous devez être connecté pour accéder au chatbot");
        }
    }

    private void animateText(TextArea textArea, String text) {
        final StringBuilder displayedText = new StringBuilder(textArea.getText()); // Garde le texte existant
        Timeline timeline = new Timeline();

        for (int i = 0; i < text.length(); i++) {
            final int index = i;
            KeyFrame keyFrame = new KeyFrame(Duration.millis(50 * (i + 1)), event -> {
                displayedText.append(text.charAt(index));
                textArea.setText(displayedText.toString());
            });
            timeline.getKeyFrames().add(keyFrame);
        }

        timeline.play();
    }
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String getChatbotResponse(String message) {
        String apiKey = dotenv.get("OPENAI_API_KEY");
        String apiUrl = dotenv.get("OPENAI_API_URL");

        if (apiKey == null || apiUrl == null) {
            return "Error: API credentials not found in environment variables. Please check your Configuration file.";
        }

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject data = new JSONObject();
            data.put("model", "gpt-4");
            data.put("messages", new org.json.JSONArray()
                    .put(new JSONObject().put("role", "user").put("content", message)));

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = data.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de la communication avec l'API: " + e.getMessage();
        }
    }
}
