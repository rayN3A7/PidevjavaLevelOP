package tn.esprit.Controllers.forum;

import tn.esprit.Models.Games;
import tn.esprit.Services.GamesService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class AddGameController {

    private GamesService gamesService = new GamesService();

    @FXML
    private TextField gameNameField;


    @FXML
    private void handleAddGame(ActionEvent event) {
        String gameName = gameNameField.getText().trim();
        if (gameName.isEmpty()) {
            showAlert("Erreur", "Le nom du jeu ne peut pas être vide.");
            return;
        }
        Games newGame = new Games();
        newGame.setGame_name(gameName);

         gamesService.add(newGame);
            showAlert("Succès", "Jeu ajouté avec succès !");
            gameNameField.clear();

    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
