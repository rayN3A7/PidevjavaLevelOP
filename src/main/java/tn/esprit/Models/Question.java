package tn.esprit.Models;

import java.sql.Timestamp;

public class Question {
    private int question_id;
    private String title;
    private String content;
    private Games game;
    private Utilisateur user;
    private int Votes;
    private Timestamp created_at;

    public Question() {
    }

    public Question(int id, String title, String content, int votes, Games game, Utilisateur user) {
        this.question_id = id;
        this.title = title;
        this.content = content;
        this.Votes = votes;
        this.game = game;
        this.user = user;
    }


    public Question(int question_id, String title, String content, Games game, Utilisateur user, int votes, Timestamp created_at) {
        this.question_id = question_id;
        this.title = title;
        this.content = content;
        this.game = game;
        this.user = user;
        this.Votes = votes;
        this.created_at = created_at;
    }

    public Question(String title, String content, Games game, Utilisateur user, int votes, Timestamp created_at) {
        this.title = title;
        this.content = content;
        this.game = game;
        this.user = user;
        this.Votes = 0;
        this.created_at = created_at;
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

    public int getVotes() {
        return Votes;
    }

    public void setVotes(int votes) {
        this.Votes = votes;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }
    public void upvote() {
        if (this.Votes == 0) {
            this.Votes++;
        }

    }

    public void downvote() {
        if (this.Votes > 0) {
            this.Votes--;
        }
    }



    @Override
    public String toString() {
        return "Question{" +
                "question_id=" + question_id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", game=" + game +
                ", user=" + user +
                ", Votes=" + Votes +
                ", created_at=" + created_at +
                '}';
    }
}
