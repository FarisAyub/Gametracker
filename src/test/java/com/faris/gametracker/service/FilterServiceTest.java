package com.faris.gametracker.service;

import com.faris.gametracker.model.Game;
import com.faris.gametracker.model.UserGame;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FilterServiceTest {

    private FilterService filterService = new FilterService();

    private List<UserGame> userGames;
    private List<Game> games;
    private Set<Long> userGameIds;

    @BeforeEach
    public void setup() {
        games = Arrays.asList(
                new Game("url", "The Witcher 3", LocalDate.of(2001, Month.MAY, 5), "CD Projekt Red", "CDPR"),
                new Game("url", "Elden Ring", LocalDate.of(2005, Month.FEBRUARY, 6), "FromSoftware", "FS"),
                new Game("url", "Valheim", LocalDate.of(2002, Month.JANUARY, 7), "Iron Gate", "IG")
        );
        // Manually set id's
        games.get(0).setId(1L);
        games.get(1).setId(2L);
        games.get(2).setId(5L);


        userGames = Arrays.asList(
                new UserGame(games.get(0), 5, "Good"),
                new UserGame(games.get(1), 4, "Great"),
                new UserGame(games.get(2), 3, "Okay")
        );

        userGameIds = new HashSet<>(Arrays.asList(1L, 2L, 3L)); // Set that tracks the id's of games on the user's list (only 2 from our games list)
    }

    // =========================
    // GAME SORT/FILTERING TESTS
    // =========================

    @Test
    public void filterByInList_ShouldReturnAllGamesInList() {
        String filterList = "inList";

        List<Game> filtered = filterService.filterByInList(games, filterList, userGameIds);

        assertEquals(2, filtered.size()); // Return 2 games in list: "The Witcher 3" and "Elden ring"
    }

    @Test
    public void filterByInList_ShouldReturnAllGamesNotInList() {
        String filterList = "notInList";

        List<Game> filtered = filterService.filterByInList(games, filterList, userGameIds);

        assertEquals(1, filtered.size()); // Return 1 game not in list: "Valheim"
    }

    @Test
    public void filterByInList_NullfilterList_ShouldReturnOriginalList() {

        List<Game> filtered = filterService.filterByInList(games, null, userGameIds);

        assertEquals(games, filtered); // Original list same as filtered list
    }

    @Test
    public void filterByInList_NoGamesInUserList_ShouldReturnEmptyList() {
        String filterList = "inList";
        Set<Long> emptySet = new HashSet<>();

        List<Game> filtered = filterService.filterByInList(games, filterList, emptySet);

        assertEquals(0, filtered.size()); // Empty list
    }

    @Test
    public void filterBySearch_ShouldReturnFilteredList() {
        String filterSearch = "Valheim";

        List<Game> filtered = filterService.filterBySearch(games, filterSearch);

        assertEquals(1, filtered.size()); // 1 game contains "Valheim"
    }

    @Test
    public void filterBySearch_NoGameMatch_ShouldReturnEmptyList() {
        String filterSearch = "test";

        List<Game> filtered = filterService.filterBySearch(games, filterSearch);

        assertEquals(0, filtered.size()); // No game matches "test" as a substring
    }

    @Test
    public void sortGames_ShouldfilterSortTitle() {
        String filterSort = "title";

        List<Game> sorted = filterService.filterSort(games, filterSort);

        // Sorted A-Z
        assertEquals("Elden Ring", sorted.get(0).getTitle());
        assertEquals("The Witcher 3", sorted.get(1).getTitle());
        assertEquals("Valheim", sorted.get(2).getTitle());
    }

    @Test
    public void sortGames_ShouldfilterSortReleaseDate() {
        String filterSort = "releaseDate";

        List<Game> sorted = filterService.filterSort(games, filterSort);

        assertEquals("The Witcher 3", sorted.get(0).getTitle()); // 2001
        assertEquals("Valheim", sorted.get(1).getTitle()); // 2002
        assertEquals("Elden Ring", sorted.get(2).getTitle()); // 2005
    }

    @Test
    public void sortGames_BlankfilterSort_ShouldReturnOriginalList() {
        String filterSort = "";

        List<Game> sorted = filterService.filterSort(games, filterSort);

        Assertions.assertEquals(games, sorted); // Original list same as filtered list
    }

    // =============================
    // USER GAME SORT/FILTERING TESTS
    // =============================

    @Test
    public void filterBySearchUserGames_ShouldReturnFilteredList() {
        List<UserGame> filtered = filterService.filterBySearch(userGames, "witcher");
        assertEquals(1, filtered.size());
    }

    @Test
    public void filterBySearchUserGames_NoGameMatch_ShouldReturnEmptyList() {
        List<UserGame> filtered = filterService.filterBySearch(userGames, "test");
        assertEquals(0, filtered.size());
    }

    @Test
    public void filterRating_ShouldReturnFilteredList() {
        List<UserGame> filtered = filterService.filterRating(userGames, 5);
        assertEquals(1, filtered.size());
        assertEquals(5, filtered.get(0).getRating());
    }

    @Test
    public void filterRating_RatingNotInList_ShouldReturnEmptyList() {
        List<UserGame> filtered = filterService.filterRating(userGames, 0);
        assertEquals(0, filtered.size());
    }

    @Test
    public void sortUserGames_ShouldfilterSortTitle() {
        List<UserGame> sorted = filterService.filterSort(userGames, "title");
        assertEquals("Elden Ring", sorted.get(0).getGame().getTitle());
        assertEquals("The Witcher 3", sorted.get(1).getGame().getTitle());
        assertEquals("Valheim", sorted.get(2).getGame().getTitle());
    }

    @Test
    public void sortUserGames_ShouldfilterSortReleaseDate() {
        List<UserGame> sorted = filterService.filterSort(userGames, "releaseDate");
        assertEquals("The Witcher 3", sorted.get(0).getGame().getTitle());
        assertEquals("Valheim", sorted.get(1).getGame().getTitle());
        assertEquals("Elden Ring", sorted.get(2).getGame().getTitle());
    }

    @Test
    public void sortUserGames_ShouldfilterSortRating() {
        List<UserGame> sorted = filterService.filterSort(userGames, "rating");
        assertEquals(5, sorted.get(0).getRating());
        assertEquals(4, sorted.get(1).getRating());
        assertEquals(3, sorted.get(2).getRating());
    }
}
