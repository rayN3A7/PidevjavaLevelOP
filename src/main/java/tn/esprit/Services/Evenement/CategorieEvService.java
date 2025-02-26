package tn.esprit.Services.Evenement;

import tn.esprit.Interfaces.IService;

import tn.esprit.Models.Evenement.Categorieevent;
import tn.esprit.utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieEvService implements IService<Categorieevent> {

    private List<Categorieevent> categorieEvents = new ArrayList<>();
    private Statement stm;
    private Connection cnx;
    public CategorieEvService(){
        this.cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void add(Categorieevent categorieEvent) {
        String qry = "INSERT INTO `categorie_event`(`nom`, `desc_categorie_event`) VALUES ('" + categorieEvent.getNom() + "','" + categorieEvent.getDescriptionCategorie() + "')";

        try{
            stm = cnx.createStatement();
            stm.executeUpdate(qry);
        } catch (SQLException e) {
            e.getMessage();
        }

    }

    @Override
    public void update(Categorieevent categorieEvent) {
        String qry = "UPDATE categorie_event SET nom=?, desc_categorie_event=? WHERE id=?";
        try {
            PreparedStatement pre = cnx.prepareStatement(qry);
            pre.setString(1, categorieEvent.getNom());
            pre.setString(2, categorieEvent.getDescriptionCategorie());
            pre.setInt(3, categorieEvent.getId());
            pre.executeUpdate();
        } catch (SQLException e) {
            e.getMessage();
        }
    }

    @Override
    public void delete(Categorieevent categorieEvent) {
        String qry = "DELETE FROM categorie_event WHERE id = ?";
        try {
            PreparedStatement pre = cnx.prepareStatement(qry);
            pre.setInt(1, categorieEvent.getId());
            pre.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // Affichez la pile d'exceptions pour déboguer
        }
    }

    @Override
    public Categorieevent getOne(int id) {
        String qry = "Select from categorie_event where id = ?";
        try {
            PreparedStatement pre = cnx.prepareStatement(qry);
            pre.setInt(1, id);
            ResultSet rs = pre.executeQuery();
            if (rs.next()) {
                Categorieevent c = new Categorieevent();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setDescriptioncategorie(rs.getString("desc_categorie_event"));
                return c;
            }
        } catch (SQLException e) {
            e.getMessage();
        }
        return null;
    }



    @Override
    public List<Categorieevent> getAll() {
        categorieEvents.clear();
        String qry = "Select * from categorie_event";
        try {
            stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                Categorieevent c = new Categorieevent();
                c.setId(rs.getInt("id"));
                c.setNom(rs.getString("nom"));
                c.setDescriptioncategorie(rs.getString("desc_categorie_event"));
                categorieEvents.add(c);
            }
        } catch (SQLException e) {
            e.getMessage();
        }
        return categorieEvents;
    }

    public List<Categorieevent> rechercheByNom(String nom) {
        return getAll().stream()
                .filter(c -> c.getNom() != null && c.getNom().toLowerCase().contains(nom.toLowerCase()))
                .distinct()
                .toList();
    }
    public void GetByNom(String nom){
        String qry = "SELECT * FROM categorie_event WHERE nom ?";
        try {
            PreparedStatement pre = cnx.prepareStatement(qry);
            pre.setString(1,nom);
            ResultSet rs = pre.executeQuery();
            while (rs.next()) {
                Categorieevent e = new Categorieevent();
                e.setId(rs.getInt("id"));
                e.setNom(rs.getString("nom"));
                e.setDescriptioncategorie(rs.getString("desc_categorie_event"));
            }
        } catch (SQLException e) {
            e.getMessage();
        }
    }
    public int getIdCategorieEvent(String nom){
        String qry = "SELECT id FROM categorie_event where nom = ?";
        try {
            PreparedStatement pre = cnx.prepareStatement(qry);
            pre.setString(1,nom);
            ResultSet rs = pre.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.getMessage();
        }
        return 0;
    }
    public String getNomCategorieEvent(int id){
        String qry = "SELECT nom FROM categorie_event where id = ?";
        try {
            PreparedStatement pre = cnx.prepareStatement(qry);
            pre.setInt(1,id);
            ResultSet rs = pre.executeQuery();
            if (rs.next()) {
                return rs.getString("nom");
            }
        } catch (SQLException e) {
            e.getMessage();
        }
        return null;
    }
}

