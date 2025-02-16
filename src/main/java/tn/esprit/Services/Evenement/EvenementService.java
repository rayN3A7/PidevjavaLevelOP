package tn.esprit.Services.Evenement;

import tn.esprit.Interfaces.IService;
import tn.esprit.Models.Evenement.Evenement;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementService implements IService<Evenement> {
    private List<Evenement> evenements = new ArrayList<>();
    private Statement stm;
    private Connection cnx;

    public EvenementService(){
        this.cnx = MyDatabase.getInstance().getCnx();
    }
    @Override
    public void add(Evenement evenement) {
        String qry = "INSERT INTO `evenement`(`categorie_id`, `nom_event`, `max_places_event`, `date_event`, `lieu_event`) VALUES ("+evenement.getCategorie_id()+",'"+evenement.getNom_event()+"',"+evenement.getMax_places_event()+",'"+evenement.getDate_event()+"','"+evenement.getLieu_event()+"')";
        try{
            PreparedStatement st = cnx.prepareStatement(qry);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Evenement evenement) {
        String qry = "UPDATE evenement SET categorie_id=?, nom_event=?, max_places_event=?, date_event=?, lieu_event=? WHERE id=?";
        try {
            PreparedStatement st = cnx.prepareStatement(qry);
            st.setInt(1, evenement.getCategorie_id());
            st.setString(2, evenement.getNom_event());
            st.setInt(3, evenement.getMax_places_event());
            st.setDate(4, evenement.getDate_event());
            st.setString(5, evenement.getLieu_event());
            st.setInt(6, evenement.getId());
            st.executeUpdate();
        } catch (SQLException e) {
            e.getMessage();
        }
    }

    @Override
    public void delete(Evenement evenement) {
        String qry = "DELETE FROM evenement where id="+evenement.getId();
        try{
            PreparedStatement st = cnx.prepareStatement(qry);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Evenement getOne(int id) {
        String qry = "Select from evenement where id = ?";
        try {
            PreparedStatement pre = cnx.prepareStatement(qry);
            pre.setInt(1, id);
            ResultSet rs = pre.executeQuery();
            if (rs.next()) {
                Evenement e = new Evenement();
                e.setId(rs.getInt("id"));
                e.setNom_event(rs.getString("nom_event"));
                e.setMax_places_event(rs.getInt("max_places_event"));
                e.setLieu_event(rs.getString("lieu_event"));
                e.setCategorie_id(rs.getInt("categorie_id"));
                e.setDate_event(rs.getDate("date_event"));
                return e;
            }
        } catch (SQLException e) {
            e.getMessage();
        }
        return null;
    }

    @Override
    public List<Evenement> getAll() {
        String qry = "Select * from evenement";
        try {
            stm = cnx.prepareStatement(qry);
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                Evenement e = new Evenement();
                e.setId(rs.getInt("id"));
                e.setNom_event(rs.getString("nom_event"));
                e.setMax_places_event(rs.getInt("max_places_event"));
                e.setLieu_event(rs.getString("lieu_event"));
                e.setCategorie_id(rs.getInt("categorie_id"));
                e.setDate_event(rs.getDate("date_event"));
                evenements.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return evenements;
    }

    public List<Evenement> rechercheByNom(String nom) {
        List<Evenement> resultats = new ArrayList<>(); // Pour stocker les résultats trouvés
        String qry = "SELECT * FROM evenement WHERE nom_event LIKE ?";
        try {
            PreparedStatement pre = cnx.prepareStatement(qry);
            pre.setString(1, "%" + nom + "%"); // Utilise le wildcard % pour une recherche partielle
            ResultSet rs = pre.executeQuery();
            while (rs.next()) { // Parcours des résultats
                Evenement e = new Evenement();
                e.setId(rs.getInt("id"));
                e.setNom_event(rs.getString("nom_event"));
                e.setMax_places_event(rs.getInt("max_places_event"));
                e.setLieu_event(rs.getString("lieu_event"));
                e.setCategorie_id(rs.getInt("categorie_id"));
                e.setDate_event(rs.getDate("date_event"));
                resultats.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultats; // Retourne la liste des événements trouvés
    }

    public boolean reserverPlace(int clientId , int evenementId){
        String checkPlacesqry = "SELECT max_places_event FROM evenement WHERE id = ?";
        String updatePlacesQuery = "UPDATE evenement SET max_places_event = max_places_event - 1 WHERE id = ?";
        String insertReservationQuery = "INSERT INTO client_evenement (client_id, evenement_id) VALUES (?, ?)";
        try{
            PreparedStatement checkPlacesStmt = cnx.prepareStatement(checkPlacesqry);
            checkPlacesStmt.setInt(1, evenementId);
            ResultSet rs = checkPlacesStmt.executeQuery();
            if (rs.next()) {
                int placesRestantes = rs.getInt("max_places_event");
                if (placesRestantes > 0) {
                    // Étape 2 : Diminuer le nombre de places disponibles
                    PreparedStatement updatePlacesStmt = cnx.prepareStatement(updatePlacesQuery);
                    updatePlacesStmt.setInt(1, evenementId);
                    updatePlacesStmt.executeUpdate();

                    // Étape 3 : Enregistrer la réservation
                    PreparedStatement insertReservationStmt = cnx.prepareStatement(insertReservationQuery);
                    insertReservationStmt.setInt(1, clientId);
                    insertReservationStmt.setInt(2, evenementId);
                    insertReservationStmt.executeUpdate();

                    System.out.println("Réservation effectuée avec succès !");
                    return true;
                } else {
                    System.out.println("Aucune place disponible pour cet événement.");
                    return false;
                }
            } else {
                System.out.println("Événement introuvable.");
                return false;
            }
        } catch (SQLException e) {
            e.getMessage();
            return false;
        }
    }
    List<Evenement> getEvenementsByNom(String nom){
        List<Evenement> resultats = new ArrayList<>();
        String qry = "Select * from evenement where nom_event = LIKE ?";
        try{
            PreparedStatement pre = cnx.prepareStatement(qry);
            pre.setString(1, "%" + nom + "%");
            ResultSet rs = pre.executeQuery();
            while(rs.next()){
                Evenement e = new Evenement();
                e.setId(rs.getInt("id"));
                e.setNom_event(rs.getString("nom_event"));
                e.setMax_places_event(rs.getInt("max_places_event"));
                e.setLieu_event(rs.getString("lieu_event"));
                e.setCategorie_id(rs.getInt("categorie_id"));
                e.setDate_event(rs.getDate("date_event"));
                resultats.add(e);
            }
        }catch(SQLException e){
            e.getMessage();
        }
        return resultats;
    }
    List<Evenement> getEvenementsByDate(Date date){
        List<Evenement> resultats = new ArrayList<>();
        String qry = "Select * from evenement where date_event = ?";
        try{
            PreparedStatement pre = cnx.prepareStatement(qry);
            pre.setDate(1, date);
            ResultSet rs = pre.executeQuery();
            while(rs.next()){
                Evenement e = new Evenement();
                e.setId(rs.getInt("id"));
                e.setNom_event(rs.getString("nom_event"));
                e.setMax_places_event(rs.getInt("max_places_event"));
                e.setLieu_event(rs.getString("lieu_event"));
                e.setCategorie_id(rs.getInt("categorie_id"));
                e.setDate_event(rs.getDate("date_event"));
                resultats.add(e);
            }
        }catch(SQLException e){
            e.getMessage();
        }
        return resultats;
    }
    List<Evenement> getEvenementsByLieu(String lieu){
        List<Evenement> resultats = new ArrayList<>();
        String qry = "Select * from evenement where lieu_event = LIKE ?";
        try{
            PreparedStatement pre = cnx.prepareStatement(qry);
            pre.setString(1, "%" + lieu + "%");
            ResultSet rs = pre.executeQuery();
            while(rs.next()){
                Evenement e = new Evenement();
                e.setId(rs.getInt("id"));
                e.setNom_event(rs.getString("nom_event"));
                e.setMax_places_event(rs.getInt("max_places_event"));
                e.setLieu_event(rs.getString("lieu_event"));
                e.setCategorie_id(rs.getInt("categorie_id"));
                e.setDate_event(rs.getDate("date_event"));
                resultats.add(e);
            }
        }catch(SQLException e){
            e.getMessage();
        }
        return resultats;
    }

}