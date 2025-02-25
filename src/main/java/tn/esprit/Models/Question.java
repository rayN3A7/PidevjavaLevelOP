package tn.esprit.Models;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class Question {
    private int question_id;
    private String title;
    private String content;
    private int votes;
    private Games game;
    private Utilisateur user;
    private Timestamp date;
    private String mediaPath;
    private String mediaType;
    private Map<String, Integer> reactions = new HashMap<>();
    private String userReaction;


    public Question() {
        this.reactions = new HashMap<>();
    }

    public Question(String title, String content, Games game, Utilisateur user, int votes, Timestamp date, String mediaPath, String mediaType) {
        this.title = title;
        this.content = content;
        this.game = game;
        this.user = user;
        this.votes = votes;
        this.date = date;
        this.mediaPath = mediaPath;
        this.mediaType = mediaType;
        this.reactions = new HashMap<>();
    }

    public Question(int question_id, String title, String content, int votes, Games game, Utilisateur user) {
        this.question_id = question_id;
        this.title = title;
        this.content = content;
        this.votes = votes;
        this.game = game;
        this.user = user;
        this.reactions = new HashMap<>();
    }


    public int getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(int question_id) {
        this.question_id = question_id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public int getVotes() {
        return votes;
    }
    public void setVotes(int votes) {
        this.votes = votes;
    }
    public Games getGame() {
        return game;
    }
    public void setGame(Games game) {
        this.game = game;
    }
    public Utilisateur getUser() {
        return user;
    }
    public void setUser(Utilisateur user) {
        this.user = user;
    }
    public Timestamp getDate() {
        return date;
    }
    public void setDate(Timestamp date) {
        this.date = date;
    }
    public String getMediaPath() {
        return mediaPath;
    }
    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
    }
    public String getMediaType() {
        return mediaType;
    }
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
    public Map<String, Integer> getReactions() {
        return reactions;
    }
    public void setReactions(Map<String, Integer> reactions) {
        this.reactions = reactions;
    }
    public String getUserReaction() {
        return userReaction;
    }
    public void setUserReaction(String userReaction) {
        this.userReaction = userReaction;
    }
}