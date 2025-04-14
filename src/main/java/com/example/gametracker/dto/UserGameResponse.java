package com.example.gametracker.dto;

import java.time.LocalDate;

public class UserGameResponse {

    private Long id;           // The ID of the UserGame entry
    private String url;         // The image url of the game
    private String title;      // The title of the game
    private String developer;  // The developer of the game
    private String publisher;  // The publisher of the game
    private LocalDate releaseDate;  // The release date of the game
    private int rating;        // The rating the user gave the game
    private String note;    // The user's note on the game

    // Constructor
    public UserGameResponse(Long id, String url, String title, String developer, String publisher, LocalDate releaseDate, int rating, String note) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.developer = developer;
        this.publisher = publisher;
        this.releaseDate = releaseDate;
        this.rating = rating;
        this.note = note;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getUrl() { return url; }

    public void setUrl(String url) { this.url = url; }
}
