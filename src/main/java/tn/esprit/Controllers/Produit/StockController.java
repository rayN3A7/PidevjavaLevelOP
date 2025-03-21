package tn.esprit.Controllers.Produit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.Models.Games;
import tn.esprit.Models.Produit;
import tn.esprit.Models.Stock;
import tn.esprit.Services.GamesService;
import tn.esprit.Services.ProduitService;
import tn.esprit.Services.StockService;
import tn.esprit.utils.SessionManager;

public class StockController {
    @FXML private VBox stockContainer;
    @FXML private TextField searchField;
    @FXML private TextField txtProduit;
    @FXML private TextField txtQuantity;
    @FXML private TextField txtPrice;
    @FXML private TextField txtImage;
    @FXML private TextField txtGame;  // Ensure this matches fx:id in FXML
    @FXML private VBox editForm;

    private StockService stockService;
    private ProduitService produitService;
    private GamesService gamesService;
    private List<Stock> stocks;
    private Stock selectedStock;
    String userRole = SessionManager.getInstance().getRole().name();

    private static final String DESTINATION_DIR = "C:\\xampp\\htdocs\\img";
    private static final List<String> VALID_IMAGE_EXTENSIONS = List.of("png", "jpg", "jpeg");

    @FXML
    public void initialize() {
        stockService = new StockService();
        produitService = new ProduitService();
        gamesService = new GamesService();

        // Check if FXML elements are properly injected
        if (txtProduit == null || txtQuantity == null || txtPrice == null || txtImage == null || txtGame == null || editForm == null) {
            System.err.println("One or more @FXML elements are not injected properly. Check FXML file.");
        }

        loadStocks();
        if (editForm != null) {
            editForm.setVisible(false);
        } else {
            System.err.println("Warning: editForm is null. Check if fx:id is properly set in FXML.");
        }
    }

    private void loadStocks() {
        if (stockContainer == null) {
            System.err.println("stockContainer is null. Cannot load stocks.");
            return;
        }
        stockContainer.getChildren().clear();
        stocks = stockService.getAll();
        for (Stock stock : stocks) {
            stockContainer.getChildren().add(createStockRow(stock));
        }
    }

    private HBox createStockRow(Stock stock) {
        HBox row = new HBox(0);
        row.getStyleClass().add("platform-info");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(20);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(15);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(15);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setPercentWidth(15);
        ColumnConstraints col5 = new ColumnConstraints();
        col5.setPercentWidth(20);
        ColumnConstraints col6 = new ColumnConstraints();
        col6.setPercentWidth(15);

        gridPane.getColumnConstraints().addAll(col1, col2, col3, col4, col5, col6);

        Produit produit = produitService.getOne(stock.getProduitId());
        Label produitLabel = new Label(produit != null ? produit.getNomProduit() : "N/A");
        produitLabel.getStyleClass().addAll("info-value", "cell");
        produitLabel.setMaxWidth(Double.MAX_VALUE);
        produitLabel.setWrapText(true);

        Games game = gamesService.getOne(stock.getGamesId());
        Label gameLabel = new Label(game != null ? game.getGame_name() : "N/A");
        gameLabel.getStyleClass().addAll("info-value", "cell");
        gameLabel.setMaxWidth(Double.MAX_VALUE);
        gameLabel.setWrapText(true);

        Label quantityLabel = new Label(String.valueOf(stock.getQuantity()));
        quantityLabel.getStyleClass().addAll("info-value", "cell");
        quantityLabel.setMaxWidth(Double.MAX_VALUE);

        Label priceLabel = new Label(String.format("%d DNT", stock.getPrixProduit()));
        priceLabel.getStyleClass().addAll("info-value", "cell");
        priceLabel.setMaxWidth(Double.MAX_VALUE);

        Label imageLabel = new Label(stock.getImage());
        imageLabel.getStyleClass().addAll("info-value", "cell");
        imageLabel.setMaxWidth(Double.MAX_VALUE);
        imageLabel.setWrapText(true);

        Button editButton = new Button("Mod...");
        editButton.getStyleClass().add("buy-now-button");
        editButton.setOnAction(event -> updateStock(stock));

        Button deleteButton = new Button("Suppr...");
        deleteButton.getStyleClass().add("back-button");
        deleteButton.setOnAction(event -> deleteStock(stock));

        HBox actionsBox = new HBox(5, editButton, deleteButton);
        actionsBox.getStyleClass().add("action-buttons");
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER);

        gridPane.add(produitLabel, 0, 0);
        gridPane.add(gameLabel, 1, 0);
        gridPane.add(quantityLabel, 2, 0);
        gridPane.add(priceLabel, 3, 0);
        gridPane.add(imageLabel, 4, 0);
        gridPane.add(actionsBox, 5, 0);

        gridPane.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(gridPane, javafx.scene.layout.Priority.ALWAYS);

        row.getChildren().add(gridPane);
        return row;
    }

    @FXML
    public void searchStock() {
        if (searchField == null) {
            System.err.println("searchField is null.");
            return;
        }
        String searchText = searchField.getText().toLowerCase();
        stockContainer.getChildren().clear();

        for (Stock stock : stocks) {
            Produit produit = produitService.getOne(stock.getProduitId());
            Games game = gamesService.getOne(stock.getGamesId());
            if (String.valueOf(stock.getId()).contains(searchText) ||
                    (produit != null && produit.getNomProduit().toLowerCase().contains(searchText)) ||
                    (game != null && game.getGame_name().toLowerCase().contains(searchText)) ||
                    String.valueOf(stock.getQuantity()).contains(searchText) ||
                    String.valueOf(stock.getPrixProduit()).contains(searchText)) {
                stockContainer.getChildren().add(createStockRow(stock));
            }
        }
    }

    @FXML
    public void updateStock(Stock stock) {
        this.selectedStock = stock;
        Produit produit = produitService.getOne(stock.getProduitId());
        txtProduit.setText(produit != null ? produit.getNomProduit() : "");
        txtQuantity.setText(String.valueOf(stock.getQuantity()));
        txtPrice.setText(String.valueOf(stock.getPrixProduit()));
        txtImage.setText(stock.getImage());
        Games game = gamesService.getOne(stock.getGamesId());
        txtGame.setText(game != null ? game.getGame_name() : "");

        if (editForm != null) {
            editForm.setVisible(true);
        }
    }

    @FXML
    public void saveStockChanges() {
        try {
            if (!validateForm()) {
                showAlert(AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs obligatoires.");
                return;
            }

            String productName = txtProduit.getText().trim();
            Produit produit = findProductByName(productName);
            if (produit == null) {
                showAlert(AlertType.ERROR, "Erreur", "Produit '" + productName + "' n'existe pas.");
                return;
            }

            String gameName = txtGame.getText().trim();
            Games game = findGameByName(gameName);
            if (game == null) {
                showAlert(AlertType.ERROR, "Erreur", "Jeu '" + gameName + "' n'existe pas.");
                return;
            }

            int quantity;
            int price;
            try {
                quantity = Integer.parseInt(txtQuantity.getText().trim());
                price = Integer.parseInt(txtPrice.getText().trim());

                if (quantity < 0 || price < 0) {
                    showAlert(AlertType.ERROR, "Erreur", "La quantité et le prix doivent être positifs.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(AlertType.ERROR, "Erreur", "La quantité et le prix doivent être des nombres entiers.");
                return;
            }

            if (selectedStock != null) {
                selectedStock.setProduitId(produit.getId());
                selectedStock.setGamesId(game.getGame_id());
                selectedStock.setQuantity(quantity);
                selectedStock.setPrixProduit(price);
                selectedStock.setImage(txtImage.getText().trim());

                stockService.update(selectedStock);
                showAlert(AlertType.INFORMATION, "Succès", "Le stock a été mis à jour avec succès.");
            } else {
                Stock newStock = new Stock(
                        0,
                        produit.getId(),
                        game.getGame_id(),
                        quantity,
                        price,
                        txtImage.getText().trim()
                );

                stockService.add(newStock);
                showAlert(AlertType.INFORMATION, "Succès", "Le nouveau stock a été ajouté avec succès.");
            }

            loadStocks();
            clearFields();
            if (editForm != null) {
                editForm.setVisible(false);
            }
            selectedStock = null;

        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Erreur", "Une erreur inattendue est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Produit findProductByName(String name) {
        List<Produit> products = produitService.getAll();
        String normalizedInput = name.trim().toLowerCase();
        for (Produit produit : products) {
            String normalizedProductName = produit.getNomProduit().trim().toLowerCase();
            if (normalizedProductName.equals(normalizedInput)) {
                return produit;
            }
        }
        return null;
    }

    private Games findGameByName(String name) {
        List<Games> games = gamesService.getAll();
        String normalizedInput = name.trim().toLowerCase();
        for (Games game : games) {
            String normalizedGameName = game.getGame_name().trim().toLowerCase();
            if (normalizedGameName.equals(normalizedInput)) {
                return game;
            }
        }
        return null;
    }

    @FXML
    public void cancelEdit() {
        if (editForm != null) {
            editForm.setVisible(false);
        }
        selectedStock = null;
        clearFields();
    }

    @FXML
    public void ButtonAjouterStock() {
        selectedStock = null;
        clearFields();
        if (editForm != null) {
            editForm.setVisible(true);
        } else {
            System.err.println("editForm is null in ButtonAjouterStock.");
        }
    }

    @FXML
    public void browseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                Path destinationPath = Paths.get(DESTINATION_DIR);
                if (!Files.exists(destinationPath)) {
                    Files.createDirectories(destinationPath);
                }
                String fileName = selectedFile.getName();
                Path targetPath = destinationPath.resolve(fileName);
                Files.copy(selectedFile.toPath(), targetPath);

                txtImage.setText(fileName);
                showAlert(AlertType.INFORMATION, "Succès", "Image uploadée avec succès sous le nom : " + fileName);
            } catch (IOException e) {
                showAlert(AlertType.ERROR, "Erreur", "Échec de l'upload de l'image : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void deleteStock(Stock stock) {
        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer ce stock ?");

        if (confirmation.showAndWait().get() == javafx.scene.control.ButtonType.OK) {
            try {
                stockService.delete(stock);
                loadStocks();
                showAlert(AlertType.INFORMATION, "Succès", "Le stock a été supprimé avec succès.");
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Erreur",
                        "Une erreur est survenue lors de la suppression: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void clearFields() {
        if (txtProduit != null) txtProduit.clear();
        if (txtQuantity != null) txtQuantity.clear();
        if (txtPrice != null) txtPrice.clear();
        if (txtImage != null) txtImage.clear();
        if (txtGame != null) txtGame.clear();
    }

    private boolean validateForm() {
        return txtProduit != null && !txtProduit.getText().trim().isEmpty() &&
                txtQuantity != null && !txtQuantity.getText().trim().isEmpty() &&
                txtPrice != null && !txtPrice.getText().trim().isEmpty() &&
                txtImage != null && !txtImage.getText().trim().isEmpty() &&
                txtGame != null && !txtGame.getText().trim().isEmpty();
    }

    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void handleBack() {
        if (userRole.equals("ADMIN")) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/sidebarAdmin.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);

                Stage window = (Stage) stockContainer.getScene().getWindow();
                window.setScene(scene);
                window.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la navigation vers l'accueil");
            }
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Produit/main.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);

                Stage window = (Stage) stockContainer.getScene().getWindow();
                window.setScene(scene);
                window.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la navigation vers l'accueil");
            }
        }
    }
}