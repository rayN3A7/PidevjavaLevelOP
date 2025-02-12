package tn.esprit.test;

import tn.esprit.Models.Reservation;
import tn.esprit.Models.Session_game;
import tn.esprit.Services.ServiceReservation;
import tn.esprit.Services.ServiceSession;

import java.util.Date;

public class MainReservation {
    public static void main(String[] args) {
        // Initialisation des services
        ServiceSession serviceSession = new ServiceSession();
        ServiceReservation serviceReservation = new ServiceReservation();

        // Création d'une nouvelle session
        Session_game session = new Session_game(28,110.0, new Date(), "50 min", "Chess", 1);
        serviceSession.add(session);


        if (session.getId() > 0) {
            Reservation reservation = new Reservation(new Date(), session, 1);
            serviceReservation.add(reservation);

            // Affichage des réservations après ajout
            System.out.println("Reservations:");
            System.out.println(serviceReservation.getAll());
        } else {
            System.out.println("Erreur: L'ID de la session n'a pas été récupéré !");
        }

        // Affichage de toutes les sessions
        System.out.println("Sessions:");
        System.out.println(serviceSession.getAll());

        // Affichage de toutes les réservations
        System.out.println("Reservations avant mise à jour:");
        System.out.println(serviceReservation.getAll());

        // Mise à jour de la réservation
        /*reservation.setdate_reservation(new Date());
        reservation.setClient_id(2);
        serviceReservation.update(reservation);*/

        /* Update Session et suppression
        session.setprix(200);
        serviceSession.update(session);

        serviceSession.delete(session);*/

        System.out.println("Reservations après mise à jour:");
        System.out.println(serviceReservation.getAll());

        // Suppression de la réservation
        /*serviceReservation.delete(reservation);

        System.out.println("Reservations après suppression:");
        System.out.println(serviceReservation.getAll());*/
    }
}
