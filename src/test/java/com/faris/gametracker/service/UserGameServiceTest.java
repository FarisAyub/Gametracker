package com.faris.gametracker.service;

import com.faris.gametracker.model.Game;
import com.faris.gametracker.model.UserGame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class UserGameServiceTest {

    private List<UserGame> userGames;

    @InjectMocks
    private UserGameService userGameService;

    @BeforeEach
    public void setup() {
        // Array of user game entries to test sorting and filtering
        userGames = Arrays.asList(
                new UserGame(new Game("url", "The Witcher 3", LocalDate.of(2001, Month.MAY, 5), "CD Projekt Red", "CDPR"), 5, "Good"),
                new UserGame(new Game("url", "Elden Ring", LocalDate.of(2005, Month.FEBRUARY, 6), "FromSoftware", "FS"), 4, "Great"),
                new UserGame(new Game("url", "Valheim", LocalDate.of(2002, Month.JANUARY, 7), "Iron Gate", "IG"), 3, "Okay")
        );
    }

    @Test
    public void filterBySearch_ShouldReturnFilteredList() {
        String searchQuery = "witcher";

        List<UserGame> filtered = userGameService.filterBySearch(userGames, searchQuery);

        assertEquals(1, filtered.size()); // Only 1 game contains witcher as a substring
    }

    @Test
    public void filterBySearch_NoGameMatch_ShouldReturnEmptyList() {
        String searchQuery = "test";

        List<UserGame> filtered = userGameService.filterBySearch(userGames, searchQuery);

        assertEquals(0, filtered.size()); // No games with test as a substring
    }

    @Test
    public void filterByRating_ShouldReturnFilteredList() {
        Integer rating = 5;

        List<UserGame> filtered = userGameService.filterByRating(userGames, rating);

        assertEquals(1, filtered.size()); // Only 1 game
        assertEquals(rating, filtered.get(0).getRating()); // Rating is 5
    }

    @Test
    public void filterByRating_RatingNotInList_ShouldReturnEmptyList() {
        Integer rating = 0;

        List<UserGame> filtered = userGameService.filterByRating(userGames, rating);

        assertEquals(0, filtered.size()); // No games have a rating of 0
    }

    @Test
    public void sortUserGames_ShouldSortByTitle() {
        String sortBy = "title";

        List<UserGame> sorted = userGameService.sortUserGames(userGames, sortBy);

        // A-Z
        assertEquals("Elden Ring", sorted.get(0).getGame().getTitle());
        assertEquals("The Witcher 3", sorted.get(1).getGame().getTitle());
        assertEquals("Valheim", sorted.get(2).getGame().getTitle());
    }

    @Test
    public void sortUserGames_ShouldSortByReleaseDate() {
        String sortBy = "releaseDate";

        List<UserGame> sorted = userGameService.sortUserGames(userGames, sortBy);

        assertEquals("The Witcher 3", sorted.get(0).getGame().getTitle()); // 2001
        assertEquals("Valheim", sorted.get(1).getGame().getTitle()); // 2002
        assertEquals("Elden Ring", sorted.get(2).getGame().getTitle()); // 2005
    }

    @Test
    public void sortUserGames_ShouldSortByRating() {
        String sortBy = "rating";

        List<UserGame> sorted = userGameService.sortUserGames(userGames, sortBy);

        assertEquals(5, sorted.get(0).getRating()); // Witcher 3, rating of 5
        assertEquals(4, sorted.get(1).getRating()); // Elden Ring, rating of 4
        assertEquals(3, sorted.get(2).getRating()); // Valheim, rating of 3
    }

}
