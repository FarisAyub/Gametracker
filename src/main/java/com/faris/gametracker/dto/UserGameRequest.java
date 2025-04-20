package com.faris.gametracker.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class UserGameRequest {
    private Long gameId;

    // Rating must be between 1-5
    @Min(1)
    @Max(5)
    private int rating;

    // Note must be 255 characters max
    @Size(max = 255)
    private String note;

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
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
}
