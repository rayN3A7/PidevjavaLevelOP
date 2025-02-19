package tn.esprit.Controllers.Coach;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.Models.Role;
import tn.esprit.Models.Session_game;
import tn.esprit.Models.Utilisateur;
import tn.esprit.Services.ServiceSession;
import tn.esprit.Services.UtilisateurService;
import tn.esprit.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class SearchSessionController {
    @FXML
    private ComboBox<String> coachIdField;
    @FXML
    private Button searchButton;
    @FXML
    private Label resultLabel;
    @FXML private TextArea sessionsTextArea;
    private List<Utilisateur> Coach = new ArrayList<>();

    String userRole = SessionManager.getInstance().getRole().name();

    private final ServiceSession serviceSession = new ServiceSession();
    private UtilisateurService us = new UtilisateurService();

    @FXML
    private void initialize() {
        GetCoach();
    }

    public void GetCoach(){
        Coach = us.getByRole("coach");
        List<String> lcoach =  Coach.stream().map(Utilisateur::getNom)
                .toList();
        coachIdField.getItems().setAll(lcoach);
    }
    @FXML
    private void showSessions() {
        List<Session_game> sessions = serviceSession.getAll();
        StringBuilder sessionListText = new StringBuilder();

        for (Session_game session : sessions) {
            sessionListText.append("Jeu: ").append(session.getGame())
                    .append("\nPrix: ").append(session.getprix()).append(" DT")
                    .append("\nDurée: ").append(session.getduree_session())
                    .append("\n--------------------\n");
        }
        sessionsTextArea.setText(sessionListText.toString());
    }


    @FXML
    private void searchSessionsByCoach() {
        try {
            String Coachname = coachIdField.getValue().toString();
            Utilisateur selectedCoach = Coach.stream()
                    .filter(coach -> coach.getNom().equals(Coachname))
                    .findFirst()
                    .orElse(null);
            Utilisateur e1 = us.getByEmail(selectedCoach.getEmail());
            if (selectedCoach != null) {
                List<Session_game> sessions = serviceSession.getSessionsByCoachId(e1.getId());

                if (sessions.isEmpty()) {
                    resultLabel.setText("Aucune session trouvée pour ce coach.");
                } else {
                    StringBuilder resultText = new StringBuilder();
                    for (Session_game session : sessions) {
                        resultText.append("Session ID: ").append(session.getId())
                                .append(", Prix: ").append(session.getprix())
                                .append(", Durée: ").append(session.getduree_session())
                                .append(", Jeu: ").append(session.getGame())
                                .append("\n");
                    }
                    resultLabel.setText(resultText.toString());
                }
            } else {
                resultLabel.setText("Aucun coach sélectionné.");
            }
        } catch (NumberFormatException e) {
            resultLabel.setText("Veuillez entrer un ID valide !");
        }
    }
    @FXML
    private void session(ActionEvent event)throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/session.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);


        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }
    @FXML
    private void reservation(ActionEvent event)throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/verifier_reservation.fxml"));
        Parent signInRoot = loader.load();
        Scene signInScene = new Scene(signInRoot);


        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setScene(signInScene);
        window.show();
    }
    @FXML
    private void Coach(ActionEvent event)throws Exception{
        if(userRole == "COACH") {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Coach/SessionManagement.fxml"));
            Parent signInRoot = loader.load();
            Scene signInScene = new Scene(signInRoot);


            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(signInScene);
            window.show();
        }
    }



}
