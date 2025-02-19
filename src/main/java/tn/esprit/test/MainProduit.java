package tn.esprit.test;

import tn.esprit.Models.*;
import tn.esprit.Services.*;

public class MainProduit {
    public static void main(String[] args) {
     /*   // Initialisation des services
        ProduitService produitService = new ProduitService();
        GamesService gameService = new GamesService();
        StockService stockService = new StockService();
        CommandeService commandeService = new CommandeService();
        Games game = new Games(1, "FIFA 45");
        //Produit produit = new Produit(2, "LASSSTTg TESTTTTTTT", "GTA 5 STEAM key", 10);
        // Test CRUD pour Produit
        System.out.println("========== TEST CRUD PRODUIT ==========");
        Produit produit = new Produit(5, "LASSSTTg TESTTTTTTT", "GTA 5 STEAM key", 10);

        System.out.println("--- Create Produit ---");
        produitService.add(produit);

        System.out.println("--- Read Produit ---");
        System.out.println("Tous les produits:");
        produitService.getAll().forEach(System.out::println);

        System.out.println("--- Update Produit ---");
        produit.setScore(88);
        produitService.update(produit);
        System.out.println("Produit modifié: " + produitService.getOne(produit.getId()));
// Test CRUD pour Game
        System.out.println("\n========== TEST CRUD GAME ==========");
        Games game = new Games(6, "FIFA 4555");

        System.out.println("--- Create Game ---");
        gameService.add(game);

        System.out.println("--- Read Game ---");
        System.out.println("Tous les jeux:");
        gameService.getAll().forEach(System.out::println);

        System.out.println("--- Update Game ---");
        game.setGame_name("Fc 28");
        gameService.update(game);
        System.out.println("Jeu modifié: " + gameService.getOne(game.getGame_id()));
        // Test CRUD pour Stock
        System.out.println("\n========== TEST CRUD STOCK ==========");
        Stock stock = new Stock(6, produit.getId(), game.getGame_id(), 100, 49, "keeeeeeeeeeey");

       // System.out.println("--- Create Stock ---");
       // stockService.add(stock);

        System.out.println("--- Read Stock ---");
        System.out.println("Tous les stocks:");
        stockService.getAll().forEach(System.out::println);

        Stock stockToUpdate = stockService.getOne(stock.getId());
        if (stockToUpdate != null) {
            stockToUpdate.setQuantity(8);
            stockToUpdate.setPrixProduit(549);
            stockService.update(stockToUpdate);
            System.out.println("Stock après modification: " + stockService.getOne(stockToUpdate.getId()));
        }
        // Test CRUD pour Commande
        System.out.println("\n========== TEST CRUD COMMANDE ==========");
        Commande commande = new Commande(1, 1, produit.getId(), "En cours");

        System.out.println("--- Create Commande ---");
        commandeService.add(commande);

        System.out.println("--- Read Commande ---");
        System.out.println("Toutes les commandes:");
        commandeService.getAll().forEach(System.out::println);

        System.out.println("--- Update Commande ---");
        commande.setStatus("Livrée");
        commandeService.update(commande);
        System.out.println("Commande modifiée: " + commandeService.getOne(commande.getId()));

        // Suppression dans l'ordre pour respecter les contraintes de clé étrangère
       System.out.println("\n========== SUPPRESSION ==========");
        System.out.println("--- Delete Commande ---");
        commandeService.delete(commande);

        System.out.println("--- Delete Stock ---");
        stockService.delete(stock);

        System.out.println("--- Delete Game ---");
        gameService.delete(game);

        System.out.println("--- Delete Produit ---");
        produitService.delete(produit);

        // Vérification finale
        System.out.println("\n========== VERIFICATION FINALE ==========");
        System.out.println("Produits restants: " + produitService.getAll().size());
        System.out.println("Jeux restants: " + gameService.getAll().size());
        System.out.println("Stocks restants: " + stockService.getAll().size());
        System.out.println("Commandes restantes: " + commandeService.getAll().size());
    */
    }

}