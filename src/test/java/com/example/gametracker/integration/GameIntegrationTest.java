package com.example.gametracker.integration;

import com.example.gametracker.model.Game;
import com.example.gametracker.model.UserGame;
import com.example.gametracker.repository.GameRepository;
import com.example.gametracker.repository.UserGameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class GameIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserGameRepository userGameRepository;

    @BeforeEach
    public void setup() {
        // Set up fresh database for each test
        userGameRepository.deleteAll();
        gameRepository.deleteAll();

        List<Game> games = Arrays.asList(
                new Game("url", "The Witcher 3", LocalDate.of(2001, Month.MAY, 5), "CD Projekt Red", "CDPR"),
                new Game("url", "Elden Ring", LocalDate.of(2005, Month.FEBRUARY, 6), "FromSoftware", "FS"),
                new Game("url", "Valheim", LocalDate.of(2002, Month.JANUARY, 7), "Iron Gate", "IG")
        );

        gameRepository.saveAll(games); // Add 3 games to database

        UserGame userGame = new UserGame(games.get(0), 5, "Amazing!");
        userGameRepository.save(userGame); // Add 1 game to user's game list
    }


    /**
     * Creates a list of games with a size based on passed in value
     *
     * @param j The size of the list of games that will be returned
     * @return
     */
    private List<Game> createGamesList(int j) {
        List<Game> games = new ArrayList<>();
        for (int i = 0; i < j; i++) {
            games.add(
                    new Game("url","Title", LocalDate.of(2025, Month.JANUARY, 1),"Developer","Publisher")
            );
        }
        return games;
    }

    @Test
    public void gamesPage_GamesExist_ShouldReturnGames() throws Exception {
        mockMvc.perform(get("/games"))
                .andExpect(status().isOk())
                .andExpect(view().name("games"))
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("games", hasSize(3)));
    }

    @Test
    public void gamesPage_InvalidPage_ShouldReturnEmpty() throws Exception {
        mockMvc.perform(get("/games").param("page", "10"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("games", hasSize(0)));
    }

    @Test
    public void gamesPage_LargeDataSet_ShouldReturnGames() throws Exception {
        gameRepository.saveAll(createGamesList(1000)); // Adds 1000 games to database
        mockMvc.perform(get("/games"))
                .andExpect(status().isOk())
                .andExpect(view().name("games"))
                .andExpect(model().attribute("games", hasSize(18))); // Max size per page
    }

    @Test
    public void gamesPage_SecondPage_ShouldReturnSmallerSetGames() throws Exception {
        gameRepository.saveAll(createGamesList(20)); // Adds 20 games to database
        mockMvc.perform(get("/games").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("games"))
                .andExpect(model().attribute("games", hasSize(5))); // 23 games, 18 per page, so second page should be 5
    }

    @Test
    public void gamesPage_SearchResultExists_ShouldReturnFilteredResults() throws Exception {
        mockMvc.perform(get("/games").param("searchQuery", "witcher"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attribute("searchQuery", "witcher"))
                .andExpect(model().attribute("games", hasSize(1))); // 1 game match
    }

    @Test
    public void gamesPage_SearchResultNoResult_ShouldReturnFilteredResults() throws Exception {
        mockMvc.perform(get("/games").param("searchQuery", "terraria"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attribute("searchQuery", "terraria"))
                .andExpect(model().attribute("games", hasSize(0))); // No games match
    }

    @Test
    public void gamesPage_SearchBlank_ShouldReturnAllGames() throws Exception {
        mockMvc.perform(get("/games").param("searchQuery", ""))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attribute("searchQuery", ""))
                .andExpect(model().attribute("games", hasSize(3))); // No games match
    }

    @Test
    public void gamesPage_FilterByInList_ShouldReturnFilteredResults() throws Exception {
        mockMvc.perform(get("/games").param("showFilter", "inList"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attribute("showFilter", "inList"))
                .andExpect(model().attribute("games", hasSize(1))); // Should return 1 game, witcher
    }

    @Test
    public void gamesPage_FilterByNotInList_ShouldReturnFilteredResults() throws Exception {
        mockMvc.perform(get("/games").param("showFilter", "notInList"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attribute("showFilter", "notInList"))
                .andExpect(model().attribute("games", hasSize(2))); // Should return 2 games not in user's list
    }

    @Test
    public void gamesPage_FilterByInvalidOption_ShouldReturnUnfilteredResults() throws Exception {
        mockMvc.perform(get("/games").param("showFilter", "testFilterOption"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attribute("showFilter", "testFilterOption"))
                .andExpect(model().attribute("games", hasSize(3))); // Should return original list no filter
    }

    @Test
    public void gamesPage_SortByTitle_ShouldReturnSortedResults() throws Exception {
        mockMvc.perform(get("/games").param("sortBy", "title"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attribute("sortBy", "title"))
                .andExpect(model().attribute("games", hasSize(3)))
                .andExpect(model().attribute("games", contains( // Make sure games are ordered
                        hasProperty("title", is("Elden Ring")),
                        hasProperty("title", is("The Witcher 3")),
                        hasProperty("title", is("Valheim"))
                )));

    }

    @Test
    public void gamesPage_SortByReleaseDate_ShouldReturnSortedResults() throws Exception {
        mockMvc.perform(get("/games").param("sortBy", "releaseDate"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attribute("sortBy", "releaseDate"))
                .andExpect(model().attribute("games", hasSize(3)))
                .andExpect(model().attribute("games", contains(
                        hasProperty("title", is("The Witcher 3")), // 2001
                        hasProperty("title", is("Valheim")), // 2002
                        hasProperty("title", is("Elden Ring")) // 2005
                )));

    }

    @Test
    public void gamesPage_InvalidSortBy_ShouldReturnUnsorted() throws Exception {
        mockMvc.perform(get("/games").param("sortBy", "notasortoption"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attribute("sortBy", "notasortoption"))
                .andExpect(model().attribute("games", hasSize(3)))
                .andExpect(model().attribute("games", contains(
                        hasProperty("title", is("The Witcher 3")), // id 1
                        hasProperty("title", is("Elden Ring")), // id 2
                        hasProperty("title", is("Valheim")) // id 3
                )));

    }

    @Test
    public void gamesPage_SortByAndShowFilter_ShouldReturnSortedAndFilteredResults() throws Exception {
        mockMvc.perform(get("/games")
                        .param("showFilter", "notInList")
                        .param("sortBy", "releaseDate"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attribute("sortBy", "releaseDate"))
                .andExpect(model().attribute("showFilter", "notInList"))
                .andExpect(model().attribute("games", hasSize(2)))
                .andExpect(model().attribute("games", contains(
                        hasProperty("title", is("Valheim")), // 2002
                        hasProperty("title", is("Elden Ring")) // 2005
                )));
    }

    @Test
    public void gamesPage_SortByAndSearch_ShouldReturnSortedAndFilteredResults() throws Exception {
        mockMvc.perform(get("/games")
                        .param("searchQuery", "he") // Matches "witcHEr" and "valHEim"
                        .param("sortBy", "releaseDate"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attribute("sortBy", "releaseDate"))
                .andExpect(model().attribute("searchQuery", "he"))
                .andExpect(model().attribute("games", hasSize(2)))
                .andExpect(model().attribute("games", contains(
                        hasProperty("title", is("The Witcher 3")), // 2001
                        hasProperty("title", is("Valheim")) // 2002
                )));
    }

    @Test
    public void gamesPage_SearchAndShowFilter_ShouldReturnSortedAndFilteredResults() throws Exception {
        mockMvc.perform(get("/games")
                .param("searchQuery", "he") // Matches "witcHEr" and "valHEim"
                .param("showFilter", "notInList"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attribute("searchQuery", "he"))
                .andExpect(model().attribute("showFilter", "notInList"))
                .andExpect(model().attribute("games", hasSize(1)))
                .andExpect(model().attribute("games", contains(
                        hasProperty("title", is("Valheim")) // Contains he and is not in list
                )));
    }

    @Test
    public void gamesPage_SearchSortByAndShowFilter_ShouldReturnSortedAndFilteredResults() throws Exception {
        mockMvc.perform(get("/games")
                .param("searchQuery", "e") // Matches all 3 games
                .param("sortBy", "releaseDate")
                .param("showFilter", "notInList")) // Elden ring and Valheim
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attribute("searchQuery", "e"))
                .andExpect(model().attribute("showFilter", "notInList"))
                .andExpect(model().attribute("sortBy", "releaseDate"))
                .andExpect(model().attribute("games", hasSize(2)))
                .andExpect(model().attribute("games", contains(
                        hasProperty("title", is("Valheim")), // 2002
                        hasProperty("title", is("Elden Ring")) // 2005
                )));
    }

    @Test
    public void gamesPage_SearchSortByAndShowFilter_EmptyResults_ShouldReturnEmpty() throws Exception {
        mockMvc.perform(get("/games")
                .param("searchQuery", "witcher") // 1 result
                .param("sortBy", "releaseDate")
                .param("showFilter", "notInList")) //No matches
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attribute("searchQuery", "witcher"))
                .andExpect(model().attribute("showFilter", "notInList"))
                .andExpect(model().attribute("sortBy", "releaseDate"))
                .andExpect(model().attribute("games", hasSize(0)));
    }
}
