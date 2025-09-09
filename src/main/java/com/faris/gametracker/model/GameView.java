package com.faris.gametracker.model;

import java.time.LocalDate;

public interface GameView {
    String getTitle();
    String getDeveloper();
    String getPublisher();
    LocalDate getReleaseDate();

    // Default for Game model since no rating
    default Integer getRating() { return null; }
}
