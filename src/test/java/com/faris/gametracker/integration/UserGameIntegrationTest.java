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

    private UserGame userGame;
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

        // Request used to add a game to the user's game list
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
                .andExpect(model().attribute("userGames", hasSize(5)));
    }

    @Test
    public void userGamePage_SecondPage_ShouldReturnSmallerList() throws Exception {
        mockMvc.perform(get("/user-games").param("page", "1").param("size", "4")) // 4 games per page
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("userGames", hasSize(1))); // 4 per page, setup initialises 5 total, so second page should have 1
    }

    @Test
    public void userGamePage_InvalidPage_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/user-games").param("page", "55"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("currentPage", 55))
                .andExpect(model().attribute("userGames", hasSize(0)));
    }

    @Test
    public void userGamePage_AddValidUserGame_ShouldAddSuccessfully() throws Exception {
        mockMvc.perform(post("/user-games").contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Game added to your list!")); //

        mockMvc.perform(get("/user-games"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(6))); // Originally contains 5
    }

    @Test
    public void userGamePage_AddInvalidRating_ShouldNotAdd() throws Exception {
        request.setRating(11);
        mockMvc.perform(post("/user-games").contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Validation error:")));

        mockMvc.perform(get("/user-games"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(5))); // Not added
    }

    @Test
    public void userGamePage_AddInvalidNote_ShouldNotAdd() throws Exception {
        String testNote = "A".repeat(256); // Creates a string larger than 255 characters

        request.setNote(testNote); // Note longer than 255 characters

        mockMvc.perform(post("/user-games").contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Validation error:")));

        mockMvc.perform(get("/user-games"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(5))); // Not added
    }

    @Test
    public void userGamePage_AddInvalidGame_ShouldNotAdd() throws Exception {
        request.setGameId(10000L); // Game non existent

        mockMvc.perform(post("/user-games").contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Game not found.")));

        mockMvc.perform(get("/user-games"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(5))); // Not added
    }

    @Test
    public void userGamePage_AddDuplicateGame_ShouldNotAdd() throws Exception {
        request.setGameId(gameRepository.findAll().get(0).getId()); // Get id of first entry and set that as our id to update

        mockMvc.perform(post("/user-games").contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Game is already in your list.")));

        mockMvc.perform(get("/user-games"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(5))); // Not added
    }

    @Test
    public void userGamePage_UpdateValidUserGame_ShouldUpdateSuccessfully() throws Exception {
        Long id = userGameRepository.findAll().get(0).getId(); // Existing id of first entry
        request.setGameId(id);

        mockMvc.perform(put("/user-games/{id}", id).contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Game updated."));

        Optional<UserGame> game = userGameRepository.findById(id); // Get the UserGame we just updated
        assertEquals(request.getRating(), game.get().getRating()); // Make sure the UserGame has our new rating
    }

    @Test
    public void userGamePage_UpdateInvalidRating_ShouldNotUpdate() throws Exception {
        Long id = userGameRepository.findAll().get(0).getId(); // Existing id of first entry
        request.setGameId(id);
        request.setRating(11);

        mockMvc.perform(put("/user-games/{id}", id).contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid input:")));

        Optional<UserGame> game = userGameRepository.findById(id); // Get the UserGame we just updated
        assertEquals(5, game.get().getRating()); // Make sure the rating is still 5, didn't update
    }

    @Test
    public void userGamePage_UpdateInvalidNote_ShouldNotUpdate() throws Exception {
        Long id = userGameRepository.findAll().get(0).getId(); // Existing id of first entry
        String testNote = "A".repeat(256); // Creates a string larger than 255 characters

        request.setGameId(id);
        request.setNote(testNote); // Note validation requires 255 or fewer characters

        mockMvc.perform(put("/user-games/{id}", id).contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid input:")));

        Optional<UserGame> game = userGameRepository.findById(id); // Get the UserGame we just updated
        assertEquals("Amazing!", game.get().getNote()); // Make sure the note wasn't updated
    }

    @Test
    public void userGamePage_UpdateInvalidGame_ShouldNotUpdate() throws Exception {
        Long id = gameRepository.findAll().get(5).getId(); // Game exists, but not in user's game list
        request.setGameId(id);

        mockMvc.perform(put("/user-games/{id}", id).contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        Optional<UserGame> game = userGameRepository.findById(id); // Should return empty optional since id doesn't exist
        assert (game.isEmpty()); // Should be empty optional
    }

    @Test
    public void userGamePage_UpdateNonExistingUserGame_ShouldNotUpdate() throws Exception {
        Long id = 1111111L;
        request.setGameId(id); // Game does NOT exist

        mockMvc.perform(put("/user-games/{id}", id).contentType("application/json").content(jsonMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        Optional<UserGame> game = userGameRepository.findById(id); // Should return empty optional since id doesn't exist
        assert (game.isEmpty()); // Should be empty optional
    }

    @Test
    public void userGamePage_DeleteUserGame_ShouldRemoveGame() throws Exception {
        Long id = userGameRepository.findAll().get(0).getId(); // Existing id of first entry

        mockMvc.perform(delete("/user-games/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().string("Game removed from your list."));

        assertEquals(4, userGameRepository.findAll().size()); // Originally there's 5, so should be 4 after
        assertFalse(userGameRepository.findById(id).isPresent()); // Should have been removed
    }

    @Test
    public void userGamePage_DeleteNonExistingUserGame_ShouldNotRemove() throws Exception {
        Long id = 111111L;

        mockMvc.perform(delete("/user-games/{id}", id))
                .andExpect(status().isNotFound());

        assertEquals(5, userGameRepository.findAll().size()); // Size has not changed
    }


    @Test
    public void userGamesPage_SearchResultExists_ShouldReturnFilteredResults() throws Exception {
        String searchQuery = "iron";

        mockMvc.perform(get("/user-games").param("searchQuery", searchQuery))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("searchQuery", searchQuery))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(1)))
                .andExpect(model().attribute("userGames", contains(
                        hasProperty("title", is("Valheim")) // iron gate studios, should return valheim
                )));
    }

    @Test
    public void userGamesPage_SearchResultNoResult_ShouldReturnFilteredResults() throws Exception {
        String searchQuery = "terraria";

        mockMvc.perform(get("/user-games").param("searchQuery", searchQuery))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("searchQuery", searchQuery))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(0))); // No match for terraria in list
    }

    @Test
    public void userGamesPage_SearchSpecialCharacters_ShouldReturnEmptyList() throws Exception {
        String searchQuery = "$%^!";

        mockMvc.perform(get("/user-games").param("searchQuery", searchQuery))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("searchQuery", searchQuery))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(0))); // No match for special characters
    }

    @Test
    public void userGamesPage_SearchBlank_ShouldReturnAllGames() throws Exception {
        String searchQuery = "";

        mockMvc.perform(get("/user-games").param("searchQuery", searchQuery))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("searchQuery", searchQuery))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(5))); // All games in list
    }

    @Test
    public void userGamesPage_SortByTitle_ShouldReturnSortedResults() throws Exception {
        String sortBy = "title";

        mockMvc.perform(get("/user-games").param("sortBy", sortBy))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("sortBy", sortBy))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(5)))
                .andExpect(model().attribute("userGames", contains(
                        hasProperty("title", is("Elden Ring")),
                        hasProperty("title", is("The Witcher 3")),
                        hasProperty("title", is("Title 1")),
                        hasProperty("title", is("Title 2")),
                        hasProperty("title", is("Valheim"))
                )));
    }

    @Test
    public void userGamesPage_SortByReleaseDate_ShouldReturnSortedResults() throws Exception {
        String sortBy = "releaseDate";

        mockMvc.perform(get("/user-games").param("sortBy", sortBy))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("sortBy", sortBy))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(5)))
                .andExpect(model().attribute("userGames", contains(
                        hasProperty("title", is("The Witcher 3")),
                        hasProperty("title", is("Valheim")),
                        hasProperty("title", is("Elden Ring")),
                        hasProperty("title", is("Title 1")),
                        hasProperty("title", is("Title 2"))
                )));
    }

    @Test
    public void userGamesPage_SortByRating_ShouldReturnSortedResults() throws Exception {
        String sortBy = "rating";

        mockMvc.perform(get("/user-games").param("sortBy", sortBy))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("sortBy", sortBy))
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
    public void userGamesPage_InvalidSortBy_ShouldReturnUnsorted() throws Exception {
        String sortBy = "invalidSortType";

        mockMvc.perform(get("/user-games").param("sortBy", sortBy))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("sortBy", sortBy))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(5)))
                .andExpect(model().attribute("userGames", contains(
                        hasProperty("title", is("The Witcher 3")),
                        hasProperty("title", is("Elden Ring")),
                        hasProperty("title", is("Valheim")),
                        hasProperty("title", is("Title 1")),
                        hasProperty("title", is("Title 2"))
                )));
    }

    @Test
    public void userGamesPage_FilterByRating_ShouldReturnFiltered() throws Exception {
        Integer filterByRating = 5;

        mockMvc.perform(get("/user-games").param("filterByRating", String.valueOf(filterByRating)))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("filterByRating", filterByRating))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(1)))
                .andExpect(model().attribute("userGames", contains(
                        hasProperty("title", is("The Witcher 3")) // Only game with 5 rating
                )));
    }

    @Test
    public void userGamesPage_FilterByInvalidRating_ShouldReturnUnsorted() throws Exception {
        Integer filterByRating = 55;

        mockMvc.perform(get("/user-games").param("filterByRating", String.valueOf(filterByRating)))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("filterByRating", filterByRating))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(5)));
    }

    @Test
    public void userGamesPage_SearchAndFilterByRating_ShouldReturnFilteredResults() throws Exception {
        String searchQuery = "2"; // Only matches title 2
        Integer filterByRating = 1; // Matches title 1 and title 2

        mockMvc.perform(get("/user-games")
                        .param("filterByRating", String.valueOf(filterByRating))
                        .param("searchQuery", searchQuery))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("filterByRating", filterByRating))
                .andExpect(model().attribute("searchQuery", searchQuery))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(1)))
                .andExpect(model().attribute("userGames", contains(
                        hasProperty("title", is("Title 2"))
                )));

    }

    @Test
    public void userGamesPage_SearchAndSort_ShouldReturnFilteredAndSortedResults() throws Exception {
        String searchQuery = "he"; // Witcher and valheim
        String sortBy = "title";

        mockMvc.perform(get("/user-games")
                        .param("sortBy", sortBy)
                        .param("searchQuery", searchQuery))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("sortBy", sortBy))
                .andExpect(model().attribute("searchQuery", searchQuery))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(2)))
                .andExpect(model().attribute("userGames", contains(
                        hasProperty("title", is("The Witcher 3")),
                        hasProperty("title", is("Valheim"))
                )));

    }

    @Test
    public void userGamesPage_FilterByRatingAndSort_ShouldReturnFilteredAndSortedResults() throws Exception {

        String searchQuery = "developer"; // Only matches title 2
        String sortBy = "title";
        Integer filterByRating = 1; // Matches title 1 and title 2

        mockMvc.perform(get("/user-games")
                        .param("sortBy", sortBy)
                        .param("filterByRating", String.valueOf(filterByRating)))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("sortBy", sortBy))
                .andExpect(model().attribute("filterByRating", filterByRating))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(2)))
                .andExpect(model().attribute("userGames", contains(
                        hasProperty("title", is("Title 1")),
                        hasProperty("title", is("Title 2"))
                )));

    }

    @Test
    public void userGamesPage_SearchSortAndFilterByRating_ShouldReturnFilteredAndSortedResults() throws Exception {
        UserGame add = new UserGame();
        add.setGame(games.get(5));
        add.setNote("game 3");
        add.setRating(1);
        userGameRepository.save(add);

        String searchQuery = "developer";
        String sortBy = "title";
        Integer filterByRating = 1;

        mockMvc.perform(get("/user-games")
                        .param("sortBy", sortBy)
                        .param("filterByRating", String.valueOf(filterByRating))
                        .param("searchQuery", searchQuery))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("searchQuery", searchQuery))
                .andExpect(model().attribute("sortBy", sortBy))
                .andExpect(model().attribute("filterByRating", filterByRating))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(3))) // title 1, title 2 and title 3 match filters
                .andExpect(model().attribute("userGames", contains(
                        hasProperty("title", is("Title 1")),
                        hasProperty("title", is("Title 2")),
                        hasProperty("title", is("Title 3"))
                )));
    }

    @Test
    public void userGamesPage_SearchSortAndFilterByRating_EmptyResults_ShouldReturnEmpty() throws Exception {
        String searchQuery = "witcher"; // Matches "Witcher 3"
        String sortBy = "title"; // Sorts by title
        Integer filterByRating = 2; // No games contain witcher that are rating 2

        mockMvc.perform(get("/user-games")
                        .param("sortBy", sortBy)
                        .param("filterByRating", String.valueOf(filterByRating))
                        .param("searchQuery", searchQuery))
                .andExpect(status().isOk())
                .andExpect(view().name("user-games"))
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("searchQuery", searchQuery))
                .andExpect(model().attribute("sortBy", sortBy))
                .andExpect(model().attribute("filterByRating", filterByRating))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("userGames", hasSize(0))); // No matches
    }
}

