package tn.esprit.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuButton;
import javafx.stage.Stage;
import tn.esprit.utils.SessionManager;

import java.io.IOException;

public class SidebarAdminController {
    @FXML
    private MenuButton evenementMenu;
    @FXML
    public void ButtonEvenement(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/ListeEvenementAdmin.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);


        Stage window = (Stage) evenementMenu.getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }
    @FXML
    private void ButtonAjouterEvenement(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/AjouterEvenement.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);

        Stage window = (Stage) evenementMenu.getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }
    @FXML
    private void ButtonListeCategorie(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/ListeCategorieAdmin.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);

        Stage window = (Stage) evenementMenu.getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }
    @FXML
    private void ButtonAjouterCategorie(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/AjouterCategorie.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);

        Stage window = (Stage) evenementMenu.getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }
    @FXML
    private void openChatbot(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur de Home.fxml
            HomeController homeController = loader.getController();

            // Appeler la méthode du chatbot
            homeController.openChatbotDialog();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void logout(ActionEvent event){
        SessionManager.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion Utilisateur/Login/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
