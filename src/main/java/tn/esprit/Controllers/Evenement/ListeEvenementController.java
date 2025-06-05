package tn.esprit.Controllers.Evenement;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.Models.Evenement.Categorieevent;
import tn.esprit.Models.Evenement.Evenement;
import tn.esprit.Services.Evenement.CategorieEvService;
import tn.esprit.Services.Evenement.EvenementService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ListeEvenementController implements Initializable {
    @FXML
    private FlowPane eventContainer;
    @FXML
    private TextField searchField;
    @FXML
    private HBox categoryCarousel;
    
    private final EvenementService es = new EvenementService();
    private final CategorieEvService ces = new CategorieEvService();
    private Button activeCategoryButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeCategoryCarousel();
        afficherEvenements();
    }

    private void initializeCategoryCarousel() {
        categoryCarousel.getChildren().clear();
        
        // Ajouter le bouton "Tous"
        Button allButton = createCategoryButton("Tous");
        allButton.getStyleClass().add("active");
        activeCategoryButton = allButton;
        categoryCarousel.getChildren().add(allButton);

        // Récupérer et ajouter toutes les catégories
        List<Categorieevent> categories = ces.getAll();
        for (Categorieevent categorie : categories) {
            Button categoryButton = createCategoryButton(categorie.getNom());
            categoryButton.setUserData(categorie.getId());
            categoryCarousel.getChildren().add(categoryButton);
        }
    }

    private Button createCategoryButton(String categoryName) {
        Button button = new Button(categoryName);
        button.getStyleClass().add("category-item");
        button.setOnAction(e -> handleCategoryClick(button));
        
        return button;
    }

    private void handleCategoryClick(Button clickedButton) {
        // Désactiver le bouton actif précédent
        if (activeCategoryButton != null) {
            activeCategoryButton.getStyleClass().remove("active");
        }
        
        // Activer le nouveau bouton
        clickedButton.getStyleClass().add("active");
        activeCategoryButton = clickedButton;
        
        // Filtrer les événements
        if (clickedButton.getText().equals("Tous")) {
            afficherEvenements();
        } else {
            Integer categorieId = (Integer) clickedButton.getUserData();
            if (categorieId != null) {
                afficherEvenementsByCategorie(categorieId);
            }
        }
    }

    private void afficherEvenementsByCategorie(int categorieId) {
        eventContainer.getChildren().clear();
        List<Evenement> evenements = es.getEvenementsByCategorie(categorieId);

        for (Evenement event : evenements) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/EvenementCard.fxml"));
                Parent card = loader.load();
                EvenementCardController controller = loader.getController();
                controller.setData(event);

                eventContainer.getChildren().add(card);
                FlowPane.setMargin(card, new Insets(10));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void afficherEvenements() {
        eventContainer.getChildren().clear();
        List<Evenement> evenements = es.getAll();

        for (Evenement event : evenements) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/EvenementCard.fxml"));
                Parent card = loader.load();
                EvenementCardController controller = loader.getController();
                controller.setData(event);

                eventContainer.getChildren().add(card);
                FlowPane.setMargin(card, new Insets(10));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void ButtonListeCategorie(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/ListeCategorie.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);

        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }

    @FXML
    private void Search() {
        String searchText = searchField.getText().trim().toLowerCase();
        eventContainer.getChildren().clear();
        List<Evenement> filteredEvents = es.GetByNom(searchText);
        for (Evenement event : filteredEvents) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Evenement/EvenementCard.fxml"));
                Parent card = loader.load();
                EvenementCardController controller = loader.getController();
                controller.setData(event);

                eventContainer.getChildren().add(card);
                FlowPane.setMargin(card, new Insets(10));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}