package tn.esprit.test;
import tn.esprit.Models.*;
import tn.esprit.Services.*;

public class Mainquestion {
    public static void main(String[] args) {
        // Créer une instance de QuestionService
        QuestionService questionService = new QuestionService();

        // Créer un utilisateur fictif (remplacez par un utilisateur réel de la BDD si nécessaire)
        Utilisateur user = new Utilisateur(1, "test@example.com", "password", "nickname", "NomTest", 12345678, "PrenomTest", Role.CLIENT);

        // Créer un jeu fictif (remplacez par un jeu réel de la BDD si nécessaire)
        Games game = new Games(1,  "Description du jeu");

        // Créer une question
        Question question = new Question(1,"Titre de la question", "Contenu de la question", 0, game, user);

        // Ajouter la question à la base de données
        questionService.add(question);

        // Récupérer et afficher toutes les questions
        System.out.println("Liste des questions :");
        for (Question q : questionService.getAll()) {
            System.out.println(q);
        }

        // Upvote la question ajoutée
        questionService.upvoteQuestion(question.getQuestion_id());
        System.out.println("Question après upvote : " + questionService.getOne(question.getQuestion_id()));
    }
}
