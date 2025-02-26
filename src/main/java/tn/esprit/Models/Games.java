package tn.esprit.Models;

public class Games {
    private int game_id;
    private String game_name;
    private String imagePath;
    private String gameType;

    public Games(int gameId) {
        this.game_id = gameId;
    }

    public Games() {
    }

    public Games(int game_id, String game_name, String imagePath, String gameType) {
        this.game_id = game_id;
        this.game_name = game_name;
        this.imagePath = imagePath;
        this.gameType = gameType;
    }

    public Games(String game_name, String imagePath, String gameType) {
        this.game_name = game_name;
        this.imagePath = imagePath;
        this.gameType = gameType;
    }

    public int getGame_id() {
        return game_id;
    }

    public void setGame_id(int game_id) {
        this.game_id = game_id;
    }

    public String getGame_name() {
        return game_name;
    }

    public void setGame_name(String game_name) {
        this.game_name = game_name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    @Override
    public String toString() {
        return "Games{" +
                "game_id=" + game_id +
                ", game_name='" + game_name + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", gameType='" + gameType + '\'' +
                '}';
    }
}
