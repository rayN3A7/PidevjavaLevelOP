package tn.esprit.Controllers.Produit;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import tn.esprit.Models.Commande;
import tn.esprit.Models.Produit;
import tn.esprit.Services.CommandeService;
import tn.esprit.Services.ProduitService;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandeStatistiqueController {
    @FXML private BarChart<String, Number> productSalesChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    private String userRole = SessionManager.getInstance().getRole().name(); // User role to determine navigation
    private CommandeService commandeService; // Service to fetch command data
    private ProduitService produitService;   // Service to fetch product details

    @FXML
    public void initialize() {
        try {
            // Initialize the services
            commandeService = new CommandeService();
            produitService = new ProduitService();

            // Apply custom styling to chart background
            productSalesChart.setStyle("-fx-background-color: #091221; -fx-padding: 20;");
            if (productSalesChart.lookup(".chart-plot-background") != null) {
                productSalesChart.lookup(".chart-plot-background").setStyle("-fx-background-color: #091221;");
            }
            
            // Style the axes with white text and larger font
            if (xAxis != null) xAxis.setStyle("-fx-text-fill: white; -fx-tick-label-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
            if (yAxis != null) yAxis.setStyle("-fx-text-fill: white; -fx-tick-label-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
            
            // Style grid lines with subtle white
            if (productSalesChart.lookup(".chart-vertical-grid-lines") != null) {
                productSalesChart.lookup(".chart-vertical-grid-lines").setStyle("-fx-stroke: rgba(255, 255, 255, 0.1);");
            }
            if (productSalesChart.lookup(".chart-horizontal-grid-lines") != null) {
                productSalesChart.lookup(".chart-horizontal-grid-lines").setStyle("-fx-stroke: rgba(255, 255, 255, 0.1);");
            }

            // Style chart title and axis labels with larger font
            productSalesChart.setStyle("-fx-background-color: #091221; -fx-padding: 20; -fx-font-size: 16px;");
            if (productSalesChart.lookup(".chart-title") != null) {
                productSalesChart.lookup(".chart-title").setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
            }

            // Create and style the series
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Ventes par Produit");

            // Get and add the sales data
            Map<String, Integer> productSales = getMostSoldProducts();
            for (Map.Entry<String, Integer> entry : productSales.entrySet()) {
                XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
                series.getData().add(data);
            }

            productSalesChart.getData().add(series);

            // Style each bar with neon pink
            for (XYChart.Data<String, Number> item : series.getData()) {
                if (item.getNode() != null) {
                    item.getNode().setStyle("-fx-bar-fill: #ff1493;");
                }
            }

            // Style the legend and axis labels with white text and larger font
            if (productSalesChart.lookup(".chart-legend") != null) {
                productSalesChart.lookup(".chart-legend").setStyle("-fx-background-color: transparent; -fx-font-size: 14px;");
            }
            if (productSalesChart.lookup(".chart-legend-item") != null) {
                productSalesChart.lookup(".chart-legend-item").setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            }
            if (productSalesChart.lookup(".axis-label") != null) {
                productSalesChart.lookup(".axis-label").setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de l'initialisation du graphique: " + e.getMessage());
        }
    }

    // Method to fetch most sold products by name
    private Map<String, Integer> getMostSoldProducts() {
        Map<String, Integer> productSales = new HashMap<>();

        try {
            // Fetch all commands using your provided getAll() method
            List<Commande> commandes = commandeService.getAll();

            // Count sales per product for completed orders only
            for (Commande commande : commandes) {
                if ("terminé".equals(commande.getStatus())) { // Only count completed orders
                    int produitId = commande.getProduitId();
                    // Fetch product by ID using ProduitService
                    Produit produit = produitService.getOne(produitId);
                    if (produit != null) {
                        String productName = produit.getNomProduit();
                        productSales.put(productName, productSales.getOrDefault(productName, 0) + 1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la récupération des données de vente: " + e.getMessage());
        }

        return productSales;
    }

    @FXML
    private void handleBack() {
        if(userRole.equals("ADMIN")) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/sidebarAdmin.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);

                Stage window = (Stage) productSalesChart.getScene().getWindow();
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

                Stage window = (Stage) productSalesChart.getScene().getWindow();
                window.setScene(scene);
                window.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Erreur", "Erreur lors de la navigation vers l'accueil");
            }
        }
    }

    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Method to set user role if needed externally
    public void setUserRole(String role) {
        this.userRole = role;
    }
}