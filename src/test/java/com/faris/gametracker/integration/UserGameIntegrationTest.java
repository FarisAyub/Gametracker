package com.faris.gametracker.integration;

import com.faris.gametracker.dto.UserGameRequest;
import com.faris.gametracker.model.Game;
import com.faris.gametracker.model.UserGame;
import com.faris.gametracker.repository.GameRepository;
import com.faris.gametracker.repository.UserGameRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class UserGameIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserGameRepository userGameRepository;

    @Autowired
    private ObjectMapper jsonMapper;

    private List<Game> games;
    private UserGameRequest request;

    @BeforeEach
    public void setup() {
        // Set up fresh database for each test
        userGameRepository.deleteAll();
        gameRepository.deleteAll();

        games = Arrays.asList(
                new Game("url", "The Witcher 3", LocalDate.of(2001, Month.MAY, 5), "CD Projekt Red", "CDPR"),
                new Game("url", "Elden Ring", LocalDate.of(2005, Month.FEBRUARY, 6), "FromSoftware", "FS"),
                new Game("url", "Valheim", LocalDate.of(2002, Month.JANUARY, 7), "Iron Gate", "IG"),
                new Game("url", "Title 1", LocalDate.of(2007, Month.JANUARY, 7), "developer 1", "p1"),
                new Game("url", "Title 2", LocalDate.of(2008, Month.JANUARY, 7), "developer 2", "p2"),
                new Game("url", "Title 3", LocalDate.of(2009, Month.JANUARY, 7), "developer 3", "p3")
        );

        gameRepository.saveAll(games); // Add 6 games to database

        List<UserGame> userGames = Arrays.asList(
                new UserGame(games.get(0), 5, "Amazing!"),
                new UserGame(games.get(1), 4, "Good"),
                new UserGame(games.get(2), 3, "Decent"),
                new UserGame(games.get(3), 1, "Bad"),
                new UserGame(games.get(4), 1, "Terrible")
        );
        userGameRepository.saveAll(userGames); // Add 5 games to user's game list

        // Request for http requests
        request = new UserGameRequest();
        request.setGameId(gameRepository.findAll().get(5).getId());
        request.setRating(1);
        request.setNote("Valid User Game");
    }


    @Test
    public void userGamesPage_ShouldReturnUserGames() throws Exception {
        mockMvc.perform(get("/user-games"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(5))); // All games
    }

    @Test
    public void userGamePage_SecondPage_ShouldReturnSmallerList() throws Exception {
        mockMvc.perform(get("/user-games").param("page", "1").param("size", "4")) // 4 games per page
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("userGames", hasSize(1))); // 4 per page, 5 total games, second page shows 1
    }

    @Test
    public void userGamePage_InvalidPage_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/user-games").param("page", "55")) // Not enough games for page 55
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("currentPage", 55))
                .andExpect(model().attribute("userGames", hasSize(0))); // Empty page
    }

    @Test
    public void userGamePage_AddValidUserGame_ShouldAddSuccessfully() throws Exception {
        mockMvc.perform(post("/user-games").contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Game added to your list!")); // Game added to database

        mockMvc.perform(get("/user-games"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(6))); // New list contains 6 games (5 before adding)
    }

    @Test
    public void userGamePage_AddInvalidRating_ShouldNotAdd() throws Exception {
        request.setRating(11); // Rating validation is 1-5
        mockMvc.perform(post("/user-games").contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Validation error:")));

        mockMvc.perform(get("/user-games"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(5))); // Still 5 games, nothing added
    }

    @Test
    public void userGamePage_AddInvalidNote_ShouldNotAdd() throws Exception {
        String testNote = "A".repeat(256); // String larger than 255 characters

        request.setNote(testNote); // Invalid note

        mockMvc.perform(post("/user-games").contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Validation error:")));

        mockMvc.perform(get("/user-games"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(5))); // Still 5 games, nothing added
    }

    @Test
    public void userGamePage_AddInvalidGame_ShouldNotAdd() throws Exception {
        request.setGameId(10000L); // Game doesn't exist

        mockMvc.perform(post("/user-games").contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Game not found.")));

        mockMvc.perform(get("/user-games"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(5))); // Still 5 games, nothing added
    }

    @Test
    public void userGamePage_AddDuplicateGame_ShouldNotAdd() throws Exception {
        request.setGameId(gameRepository.findAll().get(0).getId()); // Get id of first game

        mockMvc.perform(post("/user-games").contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Game is already in your list.")));

        mockMvc.perform(get("/user-games"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(5))); // Still 5 games, nothing added
    }

    @Test
    public void userGamePage_UpdateValidUserGame_ShouldUpdateSuccessfully() throws Exception {
        Long id = userGameRepository.findAll().get(0).getId(); // Get id of first game
        request.setGameId(id);

        mockMvc.perform(put("/user-games/{id}", id).contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Game updated!"));

        Optional<UserGame> game = userGameRepository.findById(id); // Get the entry we just updated
        assertEquals(request.getRating(), game.get().getRating()); // Make sure the rating was changed
    }

    @Test
    public void userGamePage_UpdateInvalidRating_ShouldNotUpdate() throws Exception {
        Long id = userGameRepository.findAll().get(0).getId(); // Get id of first game
        request.setGameId(id);
        request.setRating(11); // Rating validates 1-5

        mockMvc.perform(put("/user-games/{id}", id).contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid input:")));

        Optional<UserGame> game = userGameRepository.findById(id); // Get the entry we tried to update
        assertEquals(5, game.get().getRating()); // Make sure rating was not changed
    }

    @Test
    public void userGamePage_UpdateInvalidNote_ShouldNotUpdate() throws Exception {
        Long id = userGameRepository.findAll().get(0).getId(); // Get id of first game
        String testNote = "A".repeat(256); // String larger than 255 characters

        request.setGameId(id);
        request.setNote(testNote); // Invalid note

        mockMvc.perform(put("/user-games/{id}", id).contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid input:")));

        Optional<UserGame> game = userGameRepository.findById(id); // Get the entry we tried to update
        assertEquals("Amazing!", game.get().getNote()); // Make sure the note was not changed
    }

    @Test
    public void userGamePage_UpdateInvalidGame_ShouldNotUpdate() throws Exception {
        Long id = gameRepository.findAll().get(5).getId(); // Get id of a game that exists, but isn't in user's list
        request.setGameId(id);

        mockMvc.perform(put("/user-games/{id}", id).contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Game is not in your list."));

        Optional<UserGame> game = userGameRepository.findById(id); // Should return empty optional since game doesn't exist
        assert (game.isEmpty()); // Empty optional
    }

    @Test
    public void userGamePage_UpdateNonExistingUserGame_ShouldNotUpdate() throws Exception {
        Long id = 1111111L;
        request.setGameId(id); // Game doesn't exist with id

        mockMvc.perform(put("/user-games/{id}", id).contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Game is not in your list."));

        Optional<UserGame> game = userGameRepository.findById(id); // Should return empty optional since id doesn't exist
        assert (game.isEmpty()); // Empty optional
    }

    @Test
    public void userGamePage_DeleteUserGame_ShouldRemoveGame() throws Exception {
        Long id = userGameRepository.findAll().get(0).getId(); // Get id of first game

        mockMvc.perform(delete("/user-games/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string("Game removed from your list."));

        assertEquals(4, userGameRepository.findAll().size()); // Size should be down to 4, originally 5
        assertFalse(userGameRepository.findById(id).isPresent()); // Entry should no longer exist
    }

    @Test
    public void userGamePage_DeleteNonExistingUserGame_ShouldNotRemove() throws Exception {
        Long id = 111111L; // Entry doesn't exist

        mockMvc.perform(delete("/user-games/{id}", id))
                .andExpect(status().isBadRequest());

        assertEquals(5, userGameRepository.findAll().size()); // Size is not changed
    }


    @Test
    public void userGamesPage_SearchResultExists_ShouldReturnFilteredResults() throws Exception {
        String filterSearch = "iron";

        mockMvc.perform(get("/user-games").param("filterSearch", filterSearch))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("filterSearch", filterSearch))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(1)))
                .andExpect(model().attribute("userGames", contains(
                        hasProperty("title", is("Valheim")) // Iron gate studios, returns valheim
                )));
    }

    @Test
    public void userGamesPage_SearchResultNoResult_ShouldReturnFilteredResults() throws Exception {
        String filterSearch = "terraria";

        mockMvc.perform(get("/user-games").param("filterSearch", filterSearch))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("filterSearch", filterSearch))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(0))); // No matches for terraria
    }

    @Test
    public void userGamesPage_SearchSpecialCharacters_ShouldReturnEmptyList() throws Exception {
        String filterSearch = "$%^!";

        mockMvc.perform(get("/user-games").param("filterSearch", filterSearch))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("filterSearch", filterSearch))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(0))); // No match for special characters
    }

    @Test
    public void userGamesPage_SearchBlank_ShouldReturnAllGames() throws Exception {
        String filterSearch = "";

        mockMvc.perform(get("/user-games").param("filterSearch", filterSearch))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("filterSearch", filterSearch))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(5))); // All games in list
    }

    @Test
    public void userGamesPage_filterSortTitle_ShouldReturnSortedResults() throws Exception {
        String filterSort = "title";

        mockMvc.perform(get("/user-games").param("filterSort", filterSort))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("filterSort", filterSort))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(5)))
                .andExpect(model().attribute("userGames", contains( // A-Z
                        hasProperty("title", is("Elden Ring")),
                        hasProperty("title", is("The Witcher 3")),
                        hasProperty("title", is("Title 1")),
                        hasProperty("title", is("Title 2")),
                        hasProperty("title", is("Valheim"))
                )));
    }

    @Test
    public void userGamesPage_filterSortReleaseDate_ShouldReturnSortedResults() throws Exception {
        String filterSort = "releaseDate";

        mockMvc.perform(get("/user-games").param("filterSort", filterSort))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("filterSort", filterSort))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(5)))
                .andExpect(model().attribute("userGames", contains(
                        hasProperty("title", is("The Witcher 3")), // 2001
                        hasProperty("title", is("Valheim")), // 2002
                        hasProperty("title", is("Elden Ring")), // 2005
                        hasProperty("title", is("Title 1")), // 2007
                        hasProperty("title", is("Title 2")) // 2008
                )));
    }

    @Test
    public void userGamesPage_filterSortRating_ShouldReturnSortedResults() throws Exception {
        String filterSort = "rating";

        mockMvc.perform(get("/user-games").param("filterSort", filterSort))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("filterSort", filterSort))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(5)))
                .andExpect(model().attribute("userGames", contains(
                        hasProperty("title", is("The Witcher 3")), // 5
                        hasProperty("title", is("Elden Ring")), // 4
                        hasProperty("title", is("Valheim")), // 3
                        hasProperty("title", is("Title 1")), // 2
                        hasProperty("title", is("Title 2")) // 1
                )));
    }

    @Test
    public void userGamesPage_InvalidfilterSort_ShouldReturnUnsorted() throws Exception {
        String filterSort = "invalidSortType";

        mockMvc.perform(get("/user-games").param("filterSort", filterSort))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("filterSort", filterSort))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(5)))
                .andExpect(model().attribute("userGames", contains( // Unsorted list
                        hasProperty("title", is("The Witcher 3")),
                        hasProperty("title", is("Elden Ring")),
                        hasProperty("title", is("Valheim")),
                        hasProperty("title", is("Title 1")),
                        hasProperty("title", is("Title 2"))
                )));
    }

    @Test
    public void userGamesPage_filterRating_ShouldReturnFiltered() throws Exception {
        Integer filterRating = 5;

        mockMvc.perform(get("/user-games").param("filterRating", String.valueOf(filterRating)))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("filterRating", filterRating))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(1)))
                .andExpect(model().attribute("userGames", contains(
                        hasProperty("title", is("The Witcher 3")) // Has rating of 5
                )));
    }

    @Test
    public void userGamesPage_FilterByInvalidRating_ShouldReturnUnfiltered() throws Exception {
        Integer filterRating = 55;

        mockMvc.perform(get("/user-games").param("filterRating", String.valueOf(filterRating)))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("filterRating", filterRating))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(5))); // Original list, unfiltered
    }

    @Test
    public void userGamesPage_SearchAndfilterRating_ShouldReturnFilteredResults() throws Exception {
        String filterSearch = "2"; // Only matches "title 2"
        Integer filterRating = 1; // Matches "title 1" and "title 2"

        mockMvc.perform(get("/user-games")
                        .param("filterRating", String.valueOf(filterRating))
                        .param("filterSearch", filterSearch))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("filterRating", filterRating))
                .andExpect(model().attribute("filterSearch", filterSearch))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(1)))
                .andExpect(model().attribute("userGames", contains(
                        hasProperty("title", is("Title 2"))
                )));

    }

    @Test
    public void userGamesPage_SearchAndSort_ShouldReturnFilteredAndSortedResults() throws Exception {
        String filterSearch = "he"; // Matches "The Witcher 3" and "Valheim"
        String filterSort = "title";

        mockMvc.perform(get("/user-games")
                        .param("filterSort", filterSort)
                        .param("filterSearch", filterSearch))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("filterSort", filterSort))
                .andExpect(model().attribute("filterSearch", filterSearch))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(2)))
                .andExpect(model().attribute("userGames", contains( // A-Z
                        hasProperty("title", is("The Witcher 3")),
                        hasProperty("title", is("Valheim"))
                )));

    }

    @Test
    public void userGamesPage_filterRatingAndSort_ShouldReturnFilteredAndSortedResults() throws Exception {
        String filterSort = "title";
        Integer filterRating = 1; // Matches "Title 1" and "Title 2"

        mockMvc.perform(get("/user-games")
                        .param("filterSort", filterSort)
                        .param("filterRating", String.valueOf(filterRating)))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("filterSort", filterSort))
                .andExpect(model().attribute("filterRating", filterRating))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(2)))
                .andExpect(model().attribute("userGames", contains(
                        hasProperty("title", is("Title 1")),
                        hasProperty("title", is("Title 2"))
                )));

    }

    @Test
    public void userGamesPage_SearchSortAndfilterRating_ShouldReturnFilteredAndSortedResults() throws Exception {
        UserGame add = new UserGame();
        add.setGame(games.get(5));
        add.setNote("game 3");
        add.setRating(1);
        userGameRepository.save(add); // Add new game to user's list

        String filterSearch = "developer"; // Matches "developer 1", "developer 2" and "developer 3"
        String filterSort = "title";
        Integer filterRating = 1; // Matches "Title 1", "Title 2" and "Title 3"

        mockMvc.perform(get("/user-games")
                        .param("filterSort", filterSort)
                        .param("filterRating", String.valueOf(filterRating))
                        .param("filterSearch", filterSearch))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("filterSearch", filterSearch))
                .andExpect(model().attribute("filterSort", filterSort))
                .andExpect(model().attribute("filterRating", filterRating))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(3)))
                .andExpect(model().attribute("userGames", contains( // Sorted A-Z
                        hasProperty("title", is("Title 1")),
                        hasProperty("title", is("Title 2")),
                        hasProperty("title", is("Title 3"))
                )));
    }

    @Test
    public void userGamesPage_SearchSortAndfilterRating_EmptyResults_ShouldReturnEmpty() throws Exception {
        String filterSearch = "witcher"; // Matches "Witcher 3"
        String filterSort = "title";
        Integer filterRating = 2; // No games contain witcher that have a rating of 2

        mockMvc.perform(get("/user-games")
                        .param("filterSort", filterSort)
                        .param("filterRating", String.valueOf(filterRating))
                        .param("filterSearch", filterSearch))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("filterSearch", filterSearch))
                .andExpect(model().attribute("filterSort", filterSort))
                .andExpect(model().attribute("filterRating", filterRating))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(0))); // No matches
    }
}

