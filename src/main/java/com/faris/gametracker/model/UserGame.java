package com.faris.gametracker.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;


// Controller for the user, tracks all the games that the user has completed with the details of each completion

@Entity
public class UserGame implements GameView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reference to a game that has been added to the list, non-unique meaning 1 user can have multiple games on their list
    @ManyToOne
    private Game game;
    private int rating;
    private String note;

    public UserGame() {
    }

    /**
     * @param game   Game object referring to a game
     * @param rating A rating integer from 1-5
     * @param note   A note about game, must be less than 255 characters
     */
    public UserGame(Game game, int rating, String note) {
        this.game = game;
        this.rating = rating;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String notes) {
        this.note = notes;
    }

    @Override
    public Integer getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    public String getTitle() {
        return game.getTitle();
    }

    @Override
    public String getDeveloper() {
        return game.getDeveloper();
    }

    @Override
    public String getPublisher() {
        return game.getPublisher();
    }

    @Override
    public LocalDate getReleaseDate() {
        return game.getReleaseDate();
    }
}
