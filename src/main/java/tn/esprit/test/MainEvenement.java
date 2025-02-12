package tn.esprit.test;

import tn.esprit.Models.Categorieevent;
import tn.esprit.Models.Evenement;
import tn.esprit.Services.CategorieEvService;
import tn.esprit.Services.EvenementService;

import java.sql.Date;
import java.util.List;

public class MainEvenement {
    public static void main(String[] args) {
        EvenementService es = new EvenementService();
        CategorieEvService ces = new CategorieEvService();
           Categorieevent ce = new Categorieevent(1,"c est un evenement Sportif","Sport");
           ces.add(ce);
            System.out.println(ces.getAll());
            System.out.println("Categorie event Inserer");
            ces.delete(ce);
            int categorieId = -1;
            for (Categorieevent c : ces.getAll()) {
                if (c.getNom().equals("Rendonner")) {
                    categorieId = c.getId();
                    break;
                }
            }
            Evenement e = new Evenement(1,categorieId, 1000, "Evenement de Sport", "Stade de Tunis", Date.valueOf("2025-03-20"));
            es.add(e);

            es.update(e);
            es.delete(e);
            ces.update(ce);

            System.out.println("Événement mis à jour avec succès !");
            System.out.println(es.getAll());

                //Recherche par nom d'événement
            String rechercheNom = "Sport";
            List<Evenement> evenementsTrouves = es.rechercheByNom(rechercheNom);
            if (!evenementsTrouves.isEmpty()) {
                System.out.println("Événements trouvés pour la recherche : " + rechercheNom);
                for (Evenement evenement : evenementsTrouves) {
                    System.out.println(evenement);
                }
            } else {
                System.out.println("Aucun événement trouvé pour : " + rechercheNom);
            }

        //Recherche par nom de catégorie
        List<Categorieevent> lce = ces.rechercheByNom(rechercheNom);
        if (!lce.isEmpty()) {
            System.out.println("Catehorie trouvés pour la recherche : " + rechercheNom);
            for (Categorieevent categorieevent : lce) {
                System.out.println(categorieevent);
            }
        } else {
            System.out.println("Aucun événement trouvé pour : " + rechercheNom);
        }

            boolean succes = es.reserverPlace(2,2);
            if(succes){
                System.out.println("Place reservé");}
            else{
                System.out.println("Place non reservé");
            }

    }
}
