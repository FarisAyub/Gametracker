package com.faris.gametracker.model;

import jakarta.persistence.*;


// Controller for the user, tracks all the games that the user has completed with the details of each completion

@Entity
public class UserGame {
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

    public int getRating() {
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
}
