package tn.esprit.test;
import tn.esprit.Models.Games;
import tn.esprit.Services.GamesService;

import java.util.List;

public class MainGames {
    public static void main(String[] args) {
        GamesService gamesService = new GamesService();

        // Add a new game
        Games newGame = new Games("League of Legends");
        gamesService.add(newGame);
        System.out.println("Game added successfully.");

        // Retrieve all games
        List<Games> gamesList = gamesService.getAll();
        System.out.println("List of Games:");
        for (Games game : gamesList) {
            System.out.println("ID: " + game.getGame_id() + ", Name: " + game.getGame_name());
        }

        // Retrieve a game by ID
        if (!gamesList.isEmpty()) {
            Games retrievedGame = gamesService.getOne(gamesList.get(0).getGame_id());
            if (retrievedGame != null) {
                System.out.println("Retrieved Game: " + retrievedGame.getGame_name());
            }
        }

        // Update a game
       /* if (!gamesList.isEmpty()) {
            Games gameToUpdate = gamesList.get(0);
            gameToUpdate.setGame_name("Updated Game Name");
            gamesService.update(gameToUpdate);
            System.out.println("Game updated successfully.");
        }

        // Delete a game
        if (!gamesList.isEmpty()) {
            Games gameToDelete = gamesList.get(0);
            gamesService.delete(gameToDelete);
            System.out.println("Game deleted successfully.");
        }*/
    }
}
