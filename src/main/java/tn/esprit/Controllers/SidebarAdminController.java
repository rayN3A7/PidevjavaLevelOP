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
    private MenuButton produitMenu;

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
            HomeController homeController = loader.getController();
            homeController.openChatbotDialog();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void ButtonGererProd(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit/produit_view.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage window = (Stage) evenementMenu.getScene().getWindow();
        window.setScene(scene);
        window.show();
    }

    @FXML
    private void ButtonGererStock(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit/stock_view.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage window = (Stage) evenementMenu.getScene().getWindow();
        window.setScene(scene);
        window.show();
    }

    @FXML
    private void BtnUser(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion Utilisateur/Dashboard/Dashboard.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage window = (Stage) produitMenu.getScene().getWindow();
        window.setScene(scene);
        window.show();
    }
    @FXML
    private void btnCoach(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion Utilisateur/Dashboard/DisplayCoach.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage window = (Stage) produitMenu.getScene().getWindow();
        window.setScene(scene);
        window.show();
    }
    @FXML
    private void btnDemande(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion Utilisateur/addCoach/displayDemand.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage window = (Stage) produitMenu.getScene().getWindow();
        window.setScene(scene);
        window.show();
    }

    @FXML
    private void ButtonStats(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit/CommandeStatistique.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage window = (Stage) evenementMenu.getScene().getWindow();
        window.setScene(scene);
        window.show();
    }

    @FXML
    private void ButtonGererCommande(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit/commande_view.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage window = (Stage) evenementMenu.getScene().getWindow();
        window.setScene(scene);
        window.show();
    }

    @FXML
    private void logout(ActionEvent event) {
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

    @FXML
    private void navigateToForumAdmin(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/ForumAdmin.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
    }

    @FXML
    private void navigateToAdminDashboard(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/forumUI/AdminDashboard.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(scene);
        window.show();
    }
    @FXML
    private void ButtonEntraineur(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/analytics.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);

        Stage window = (Stage) evenementMenu.getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }
    @FXML
    private void ButtonHome(ActionEvent event)throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Home.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);


        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }
}