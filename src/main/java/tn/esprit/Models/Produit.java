package tn.esprit.Models;

public class Produit {
    private int id;
    private String nomProduit;
    private String description;
    private String platform;
    private String region;
    private String type;
    private String activation_region;
    private int score;

    // Default constructor
    public Produit() {
    }

    // Constructor with all fields
    public Produit(int id, String nomProduit, String description,
                   String platform, String region, String type,
                   String activation_region, int score) {
        this.id = id;
        this.nomProduit = nomProduit;
        this.description = description;
        this.platform = platform;
        this.region = region;
        this.type = type;
        this.activation_region = activation_region;
        this.score = score;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public void setNomProduit(String nomProduit) {
        this.nomProduit = nomProduit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getActivation_region() {
        return activation_region;
    }

    public void setActivation_region(String activation_region) {
        this.activation_region = activation_region;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Produit{" +
                "id=" + id +
                ", nomProduit='" + nomProduit + '\'' +
                ", description='" + description + '\'' +
                ", platform='" + platform + '\'' +
                ", region='" + region + '\'' +
                ", type='" + type + '\'' +
                ", activation_region='" + activation_region + '\'' +
                ", score=" + score +
                '}';
    }
}