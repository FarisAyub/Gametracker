package com.example.gametracker.controller;

import com.example.gametracker.dto.UserGameRequest;
import com.example.gametracker.model.Game;
import com.example.gametracker.model.UserGame;
import com.example.gametracker.repository.GameRepository;
import com.example.gametracker.repository.UserGameRepository;
import com.example.gametracker.service.GameApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserGameController.class)
public class UserGameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameApiService gameApiService;

    @MockBean
    private GameRepository gameRepository;

    @MockBean
    private UserGameRepository userGameRepository;

    @InjectMocks
    private UserGameController userGameController;

    private UserGameRequest request; // Create an user game request to reinitialise and assign values before each test

    private List<UserGame> userGames;

    @Autowired
    private ObjectMapper objectMapper; // To convert java to JSON for http requests like post

    @BeforeEach
    public void setup() {
        // Creates a new request object before each test being run. This saves having to repeatedly remake and re-assign the request for each test
        request = new UserGameRequest();
        request.setGameId(1L);
        request.setRating(5);
        request.setNote("Test note");

        // Array of user game entries to test sorting and filtering
        userGames = Arrays.asList(
                new UserGame(new Game("url", "The Witcher 3", LocalDate.of(2001, Month.MAY, 5), "CD Projekt Red", "CDPR"), 5, "Good"),
                new UserGame(new Game("url", "Elden Ring", LocalDate.of(2005, Month.FEBRUARY, 6), "FromSoftware", "FS"), 4, "Great"),
                new UserGame(new Game("url", "Valheim", LocalDate.of(2002, Month.JANUARY, 7), "Iron Gate", "IG"), 3, "Okay")
        );
    }

    @Test
    public void getAllUserGames_ShouldReturnUserGameAndAllAttributes() throws Exception {
        Game game = new Game("url", "The Witcher 3", LocalDate.of(2001, Month.JANUARY, 15), "CD Projekt Red", "CD Projekt Red");
        game.setId(1L);
        UserGame userGame = new UserGame(game, 5, "Great!");
        userGame.setId(1L);

        when(userGameRepository.findAll()).thenReturn(List.of(userGame));

        mockMvc.perform(get("/user-games"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userGames"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("hasNext", false))
                .andExpect(model().attribute("hasPrevious", false));
    }

    @Test
    public void addUserGame_ShouldAddGame() throws Exception {
        when(gameRepository.findById(request.getGameId())).thenReturn(Optional.of(new Game())); // Simulate game with id exists
        when(userGameRepository.existsByGameId(request.getGameId())).thenReturn(false); // Simulate game is not in user's list

        mockMvc.perform(post("/user-games").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Game added to your list!"));

    }

    @Test
    public void addUserGame_NullNote_ShouldAddGame() throws Exception {
        request.setNote(null);
        when(gameRepository.findById(request.getGameId())).thenReturn(Optional.of(new Game())); // Simulate game with id exists
        when(userGameRepository.existsByGameId(request.getGameId())).thenReturn(false); // Simulate game is not in user's list

        mockMvc.perform(post("/user-games").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Game added to your list!"));

    }

    @Test
    public void addUserGame_RatingNotValid_ShouldReturnBadRequest() throws Exception {
        request.setRating(6); // Rating validation is set to be a value from 1-5

        when(gameRepository.findById(request.getGameId())).thenReturn(Optional.of(new Game())); // Simulate game with id exists

        mockMvc.perform(post("/user-games").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Validation error:")));

    }

    @Test
    public void addUserGame_NoteNotValid_ShouldReturnBadRequest() throws Exception {
        // Test note using lorem ipsum that is 256 characters
        String testNote = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis,.";

        request.setNote(testNote); // Note is larger than 256 characters

        when(gameRepository.findById(request.getGameId())).thenReturn(Optional.of(new Game())); // Simulate game with id exists

        mockMvc.perform(post("/user-games").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Validation error:")));

    }

    @Test
    public void addUserGame_DuplicateEntry_ShouldReturnConflict() throws Exception {
        when(gameRepository.findById(request.getGameId())).thenReturn(Optional.of(new Game())); // Simulate game with id exists
        when(userGameRepository.existsByGameId(request.getGameId())).thenReturn(true); // Simulate game is already in list

        // Mock a JSON post request, should return conflict error, which would return the string for when a game is already in the user's game list
        mockMvc.perform(post("/user-games").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Game is already in your list."));

    }

    @Test
    public void addUserGame_GameDoesntExist_ShouldReturnBadRequest() throws Exception {

        // Simulate that a game with the id doesn't exist, setting the optional to empty
        when(gameRepository.findById(request.getGameId())).thenReturn(Optional.empty());

        // Mock a JSON post request, should return bad request with game not found string
        mockMvc.perform(post("/user-games").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Game not found."));
    }

    @Test
    public void deleteUserGame_GameExists_ShouldDeleteGame() throws Exception {
        long gameId = 1L;

        when(userGameRepository.existsById(gameId)).thenReturn(true); // Simulate game is in list

        mockMvc.perform(delete("/user-games/{id}", gameId))
                .andExpect(status().isOk())
                .andExpect(content().string("Game removed from your list."));
    }

    @Test
    public void deleteUserGame_GameDoesntExist_ShouldReturnNotFound() throws Exception {
        long gameId = 1L;

        when(userGameRepository.existsById(gameId)).thenReturn(false); // Simulate game is not in list

        mockMvc.perform(delete("/user-games/{id}", gameId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateUserGame_UserGameExists_ShouldUpdateGame() throws Exception {
        long id = 1L;
        request.setGameId(id);

        UserGame existingUserGame = new UserGame(); // Values existing in database
        existingUserGame.setRating(2);
        existingUserGame.setNote("Original note");

        when(userGameRepository.findById(id)).thenReturn(Optional.of(existingUserGame)); // When it searches by id, it returns existing user game

        mockMvc.perform(put("/user-games/{id}", id).contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Game updated."));

        ArgumentCaptor<UserGame> captor = ArgumentCaptor.forClass(UserGame.class);
        verify(userGameRepository).save(captor.capture());

        UserGame updatedUserGame = captor.getValue();
        assertEquals(5, updatedUserGame.getRating()); // Make sure the updated game has the new rating
        assertEquals("Test note", updatedUserGame.getNote()); // Make sure the updated game has the new note
    }

    @Test
    public void updateUserGame_GameDoesntExist_ShouldReturnNotFound() throws Exception {
        long id = 1L;
        request.setGameId(id);

        when(userGameRepository.findById(request.getGameId())).thenReturn(Optional.empty()); // Simulate entry with the id doesn't exist to update

        mockMvc.perform(put("/user-games/{id}", id).contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

    }

    @Test
    public void updateUserGame_RatingNotValid_ShouldReturnBadRequest() throws Exception {
        long id = 1L;
        request.setGameId(id);
        request.setRating(6); // Invalid rating, must be 1-5

        when(userGameRepository.findById(request.getGameId())).thenReturn(Optional.of(new UserGame())); // id exists in user games list

        mockMvc.perform(put("/user-games/{id}", id).contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid input:")));
    }

    @Test
    public void updateUserGame_NoteNotValid_ShouldReturnBadRequest() throws Exception {
        long id = 1L;
        String testNote = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis,.";

        request.setGameId(id);
        request.setNote(testNote); // Note is 256 characters, validation requires 255 max

        when(userGameRepository.findById(request.getGameId())).thenReturn(Optional.of(new UserGame())); // id exists in user games list

        mockMvc.perform(put("/user-games/{id}", id).contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid input:")));
    }

    @Test
    public void filterBySearch_ShouldReturnFilteredList() {
        String searchQuery = "witcher";

        List<UserGame> filtered = userGameController.filterBySearch(userGames, searchQuery);

        assertEquals(1, filtered.size()); // Only 1 game should contain witcher as a substring
    }

    @Test
    public void filterBySearch_NoGameMatch_ShouldReturnEmptyList() {
        String searchQuery = "test";

        List<UserGame> filtered = userGameController.filterBySearch(userGames, searchQuery);

        assertEquals(0, filtered.size()); // Size should be 0 as there are are no games with test as a substring
    }

    @Test
    public void filterByRating_ShouldReturnFilteredList() {
        Integer rating = 5;

        List<UserGame> filtered = userGameController.filterByRating(userGames, rating);

        assertEquals(1, filtered.size()); // Only 1 game should have a rating of 5
    }

    @Test
    public void filterByRating_RatingNotInList_ShouldReturnEmptyList() {
        Integer rating = 0;

        List<UserGame> filtered = userGameController.filterByRating(userGames, rating);

        assertEquals(0, filtered.size()); // No games should have a rating of 0
    }

    @Test
    public void sortUserGames_ShouldSortByTitle() {
        String sortBy = "title";

        List<UserGame> sorted = userGameController.sortUserGames(userGames, sortBy);

        assertEquals("Elden Ring", sorted.get(0).getGame().getTitle());
        assertEquals("The Witcher 3", sorted.get(1).getGame().getTitle());
        assertEquals("Valheim", sorted.get(2).getGame().getTitle());
    }

    @Test
    public void sortUserGames_ShouldSortByReleaseDate() {
        String sortBy = "releaseDate";

        List<UserGame> sorted = userGameController.sortUserGames(userGames, sortBy);

        assertEquals("The Witcher 3", sorted.get(0).getGame().getTitle()); // 2001
        assertEquals("Valheim", sorted.get(1).getGame().getTitle()); // 2002
        assertEquals("Elden Ring", sorted.get(2).getGame().getTitle()); // 2005
    }

    @Test
    public void sortUserGames_ShouldSortByRating() {
        String sortBy = "rating";

        List<UserGame> sorted = userGameController.sortUserGames(userGames, sortBy);

        assertEquals(5, sorted.get(0).getRating()); // Witcher 3
        assertEquals(4, sorted.get(1).getRating()); // Elden Ring
        assertEquals(3, sorted.get(2).getRating()); // Valheim
    }

}
