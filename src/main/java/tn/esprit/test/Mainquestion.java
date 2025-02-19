package tn.esprit.test;

import tn.esprit.Models.*;
import tn.esprit.Services.*;

public class Mainquestion {
    public static void main(String[] args) {
        QuestionService questionService = new QuestionService();
        Utilisateur user = new Utilisateur(1, "test@example.com", "password", "nickname", "NomTest", 12345678, "PrenomTest", Role.CLIENT);
       Games game = new Games(2, "aaaaah");
        Question question = new Question(54, "this one is for the boys", "how to choose a champion in lol", 0, game, user);
        questionService.add(question);
        /*System.out.println("Liste des questions :");
        for (Question q : questionService.getAll()) {
            System.out.println(q);
        }*/
       /* questionService.upvoteQuestion(question.getQuestion_id());
        System.out.println("Question après upvote : " + questionService.getOne(question.getQuestion_id()));
*/


        question.setTitle("jdid yjaded");
        question.setContent("jdid yjaded");
        questionService.update(question);
        System.out.println("Question après mise à jour : " + questionService.getOne(question.getQuestion_id()));


       /* questionService.delete(question);
        System.out.println("Liste des questions après suppression :");
        for (Question q : questionService.getAll()) {
            System.out.println(q);
        }*/
    }
}
