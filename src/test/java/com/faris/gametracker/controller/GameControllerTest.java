package com.faris.gametracker.controller;

import com.faris.gametracker.model.Game;
import com.faris.gametracker.model.UserGame;
import com.faris.gametracker.repository.GameRepository;
import com.faris.gametracker.repository.UserGameRepository;
import com.faris.gametracker.service.GameApiService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameController.class)
public class GameControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameApiService gameApiService;

    @MockBean
    private GameRepository gameRepository;

    @MockBean
    private UserGameRepository userGameRepository;

    @InjectMocks
    private GameController gameController;

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
        userGameIds = new HashSet<>(Arrays.asList(1L, 2L, 3L)); // Set that tracks the id's of games on the user's list (only 2 from our games list)
    }

    @Test
    public void getAllGames_ShouldReturnGamesAndAttributes() throws Exception {
        when(gameRepository.findAll()).thenReturn(games); // When findAll is called, return games
        when(userGameRepository.findAll()).thenReturn(Collections.emptyList()); // Return empty list

        mockMvc.perform(get("/games").param("page", "0").param("size", "18"))
                .andExpect(status().isOk())
                .andExpect(view().name("games"))
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attributeExists("userGameIds"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("hasNext", false))
                .andExpect(model().attribute("hasPrevious", false));

    }

    @Test
    public void getAllGames_EmptyLists_ShouldStillReturnGamesView() throws Exception {
        when(gameRepository.findAll()).thenReturn(Collections.emptyList()); // When findall is called, return empty list
        when(userGameRepository.findAll()).thenReturn(Collections.emptyList()); // Return empty list

        mockMvc.perform(get("/games"))
                .andExpect(status().isOk())
                .andExpect(view().name("games"))
                .andExpect(model().attribute("games", Collections.emptyList()));
    }

    @Test
    public void getAllGames_InvalidPageParameter_ShouldRedirectToErrorView() throws Exception {
        when(gameRepository.findAll()).thenReturn(games);
        when(userGameRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/games").param("page", "-1")) // Invalid page
                .andExpect(status().isOk()) // Successful as exception handler redirects to error page
                .andExpect(view().name("error")); // Should be redirected to the error page
    }

    @Test
    public void getAllGames_WithOptionalSearchSortAndFilter_ShouldReturnFilteredGames() throws Exception {
        when(gameRepository.findAll()).thenReturn(games); // When findall, return our games list

        UserGame userGame = new UserGame(games.get(0), 5, "Great!");
        List<UserGame> userGames = List.of(userGame);
        when(userGameRepository.findAll()).thenReturn(userGames); // Simulate returning list of games

        mockMvc.perform(get("/games")
                        .param("searchQuery", "witcher")
                        .param("sortBy", "title")
                        .param("showFilter", "inList")
                        .param("page", "0")
                        .param("size", "18"))
                .andExpect(status().isOk())
                .andExpect(view().name("games"))
                .andExpect(model().attribute("searchQuery", "witcher"))
                .andExpect(model().attribute("sortBy", "title"))
                .andExpect(model().attribute("showFilter", "inList"))
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attributeExists("userGameIds"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("hasNext", false))
                .andExpect(model().attribute("hasPrevious", false))
                .andExpect(model().attribute("games", hasSize(1)));

    }

    @Test
    public void filterByInList_ShouldReturnAllGamesInList() {
        String showFilter = "inList";

        List<Game> filtered = gameController.filterByInList(games, showFilter, userGameIds);

        assertEquals(2, filtered.size()); // Return 2 games in list: "The Witcher 3" and "Elden ring"
    }

    @Test
    public void filterByInList_ShouldReturnAllGamesNotInList() {
        String showFilter = "notInList";

        List<Game> filtered = gameController.filterByInList(games, showFilter, userGameIds);

        assertEquals(1, filtered.size()); // Return 1 game not in list: "Valheim"
    }

    @Test
    public void filterByInList_NullShowFilter_ShouldReturnOriginalList() {

        List<Game> filtered = gameController.filterByInList(games, null, userGameIds);

        assertEquals(games, filtered); // Original list same as filtered list
    }

    @Test
    public void filterByInList_NoGamesInUserList_ShouldReturnEmptyList() {
        String showFilter = "inList";
        Set<Long> emptySet = new HashSet<>();

        List<Game> filtered = gameController.filterByInList(games, showFilter, emptySet);

        assertEquals(0, filtered.size()); // Empty list
    }

    @Test
    public void filterBySearch_ShouldReturnFilteredList() {
        String searchQuery = "Valheim";

        List<Game> filtered = gameController.filterBySearch(games, searchQuery);

        assertEquals(1, filtered.size()); // 1 game contains "Valheim"
    }

    @Test
    public void filterBySearch_NoGameMatch_ShouldReturnEmptyList() {
        String searchQuery = "test";

        List<Game> filtered = gameController.filterBySearch(games, searchQuery);

        assertEquals(0, filtered.size()); // No game matches "test" as a substring
    }

    @Test
    public void sortGames_ShouldSortByTitle() {
        String sortBy = "title";

        List<Game> sorted = gameController.sortGames(games, sortBy);

        // Sorted A-Z
        assertEquals("Elden Ring", sorted.get(0).getTitle());
        assertEquals("The Witcher 3", sorted.get(1).getTitle());
        assertEquals("Valheim", sorted.get(2).getTitle());
    }

    @Test
    public void sortGames_ShouldSortByReleaseDate() {
        String sortBy = "releaseDate";

        List<Game> sorted = gameController.sortGames(games, sortBy);

        assertEquals("The Witcher 3", sorted.get(0).getTitle()); // 2001
        assertEquals("Valheim", sorted.get(1).getTitle()); // 2002
        assertEquals("Elden Ring", sorted.get(2).getTitle()); // 2005
    }

    @Test
    public void sortGames_BlankSortBy_ShouldReturnOriginalList() {
        String sortBy = "";

        List<Game> sorted = gameController.sortGames(games, sortBy);

        Assertions.assertEquals(games, sorted); // Original list same as filtered list
    }


}
