package tn.esprit.Controllers.Utilisateur;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SideBarController implements Initializable {

    @FXML
    private Button dashboardBtn;
    @FXML
    private Button usersBtn;
    @FXML
    private Button demandeBtn;
    @FXML
    private Button logoutBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupNavigation();
    }

    private void setupNavigation() {
        dashboardBtn.setOnAction(e -> navigateTo("gestion Utilisateur/Dashboard/Dashboard", dashboardBtn));
        usersBtn.setOnAction(e -> navigateTo("gestion Utilisateur/Dashboard/Dashboard", usersBtn));
        demandeBtn.setOnAction(e -> navigateTo("gestion Utilisateur/addCoach/displayDemand", demandeBtn));
        logoutBtn.setOnAction(e -> navigateTo("gestion Utilisateur/Dashboard/DisplayCoach", logoutBtn));
    }

    private void navigateTo(String page, Button btn) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + page + ".fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading " + page + ".fxml");
        }
    }
}
