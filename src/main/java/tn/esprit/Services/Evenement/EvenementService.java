package tn.esprit.Services.Evenement;

import tn.esprit.Interfaces.IService;
import tn.esprit.Models.Evenement.Evenement;
import tn.esprit.Models.Utilisateur;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EvenementService implements IService<Evenement> {
    private List<Evenement> evenements = new ArrayList<>();
    private Statement stm;
    private Connection cnx;

    public EvenementService(){
        this.cnx = MyDatabase.getInstance().getCnx();
    }
    @Override
    public void add(Evenement evenement) {
        String qry = "INSERT INTO `evenement`(`categorie_id`, `nom_event`, `max_places_event`, `date_event`, `lieu_event`,`photo_event`) VALUES ("+evenement.getCategorie_id()+",'"+evenement.getNom_event()+"',"+evenement.getMax_places_event()+",'"+evenement.getDate_event()+"','"+evenement.getLieu_event()+"','"+evenement.getPhoto_event()+"')";
        try{
            PreparedStatement st = cnx.prepareStatement(qry);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Evenement evenement) {
        String qry = "UPDATE evenement SET categorie_id=?, nom_event=?, max_places_event=?, date_event=?, lieu_event=?,photo_event=? WHERE id=?";
        try {
            PreparedStatement st = cnx.prepareStatement(qry);
            st.setInt(1, evenement.getCategorie_id());
            st.setString(2, evenement.getNom_event());
            st.setInt(3, evenement.getMax_places_event());
            st.setTimestamp(4, evenement.getDate_event());
            st.setString(5, evenement.getLieu_event());
            st.setString(6, evenement.getPhoto_event());
            st.setInt(7, evenement.getId());
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
                e.setDate_event(rs.getTimestamp("date_event"));
                e.setPhoto_event(rs.getString("photo_event"));
                return e;
            }
        } catch (SQLException e) {
            e.getMessage();
        }
        return null;
    }

    @Override
    public List<Evenement> getAll() {
        evenements.clear();
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
                e.setDate_event(rs.getTimestamp("date_event"));
                e.setPhoto_event(rs.getString("photo_event"));
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
                e.setDate_event(rs.getTimestamp("date_event"));
                e.setPhoto_event(rs.getString("photo_event"));
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
    public List<Evenement> GetByNom(String nom) {
        return getAll().stream()
                .filter(e -> e.getNom_event() != null && e.getNom_event().toLowerCase().contains(nom.toLowerCase()))
                .distinct()
                .collect(Collectors.toList());
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
                e.setDate_event(rs.getTimestamp("date_event"));
                e.setPhoto_event(rs.getString("photo_event"));
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
                e.setDate_event(rs.getTimestamp("date_event"));
                e.setPhoto_event(rs.getString("photo_event"));
                resultats.add(e);
            }
        }catch(SQLException e){
            e.getMessage();
        }
        return resultats;
    }
    public boolean reservationExiste(int userId, int eventId) {
        String query = "SELECT COUNT(*) FROM client_evenement WHERE client_id = ? AND evenement_id = ?";
        try (
                PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public List<String[]> getListeUtilisateursReserves(int eventId) {
        List<String[]> resultats = new ArrayList<>();
        String qry = "SELECT u.nom, u.prenom, u.email FROM utilisateur u JOIN client_evenement ce ON u.id = ce.client_id WHERE ce.evenement_id = ?";
        try {
            PreparedStatement pre = cnx.prepareStatement(qry);
            pre.setInt(1, eventId);
            ResultSet rs = pre.executeQuery();
            while (rs.next()) {
                resultats.add(new String[]{rs.getString("nom"), rs.getString("prenom"), rs.getString("email")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultats;
    }
    public List<Evenement> getEvenementsProches() {
        List<Evenement> evenementsProches = new ArrayList<>();
        String qry = "SELECT * FROM evenement WHERE date_event >= NOW() ORDER BY date_event ASC LIMIT 5";
        try {
            PreparedStatement pre = cnx.prepareStatement(qry);
            ResultSet rs = pre.executeQuery();
            while (rs.next()) {
                Evenement e = new Evenement();
                e.setId(rs.getInt("id"));
                e.setNom_event(rs.getString("nom_event"));
                e.setMax_places_event(rs.getInt("max_places_event"));
                e.setLieu_event(rs.getString("lieu_event"));
                e.setCategorie_id(rs.getInt("categorie_id"));
                e.setDate_event(rs.getTimestamp("date_event"));
                e.setPhoto_event(rs.getString("photo_event"));
                evenementsProches.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return evenementsProches;
    }

}