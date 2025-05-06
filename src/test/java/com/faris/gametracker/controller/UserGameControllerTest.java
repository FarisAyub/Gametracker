package com.faris.gametracker.controller;

import com.faris.gametracker.dto.UserGameRequest;
import com.faris.gametracker.model.Game;
import com.faris.gametracker.model.UserGame;
import com.faris.gametracker.repository.GameRepository;
import com.faris.gametracker.repository.UserGameRepository;
import com.faris.gametracker.service.GameApiService;
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

    private UserGameRequest request;

    private List<UserGame> userGames;

    @Autowired
    private ObjectMapper objectMapper; // To convert java to JSON for http requests like post

    @BeforeEach
    public void setup() {
        // Request to be passed as JSON for http requests
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
        when(userGameRepository.findAll()).thenReturn(userGames); // When findAll is called, return userGames

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
        when(gameRepository.findById(request.getGameId())).thenReturn(Optional.of(new Game()));
        when(userGameRepository.existsByGameId(request.getGameId())).thenReturn(false);

        mockMvc.perform(post("/user-games").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Game added to your list!"));

    }

    @Test
    public void addUserGame_RatingNotValid_ShouldReturnBadRequest() throws Exception {
        request.setRating(6); // Rating validation expects value of 1-5

        when(gameRepository.findById(request.getGameId())).thenReturn(Optional.of(new Game())); // Simulate game with id exists

        mockMvc.perform(post("/user-games").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Validation error:")));

    }

    @Test
    public void addUserGame_NoteNotValid_ShouldReturnBadRequest() throws Exception {
        String testNote = "A".repeat(256); // String larger than 255 characters
        request.setNote(testNote);

        when(gameRepository.findById(request.getGameId())).thenReturn(Optional.of(new Game())); // Simulate game with id exists

        mockMvc.perform(post("/user-games").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Validation error:")));

    }

    @Test
    public void addUserGame_DuplicateEntry_ShouldReturnConflict() throws Exception {
        when(gameRepository.findById(request.getGameId())).thenReturn(Optional.of(new Game()));
        when(userGameRepository.existsByGameId(request.getGameId())).thenReturn(true); // Simulate game is already in list

        mockMvc.perform(post("/user-games").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict()) // Conflict as id is already in list
                .andExpect(content().string("Game is already in your list."));

    }

    @Test
    public void addUserGame_GameDoesntExist_ShouldReturnBadRequest() throws Exception {
        when(gameRepository.findById(request.getGameId())).thenReturn(Optional.empty()); // Simulate game doesn't exist

        mockMvc.perform(post("/user-games").contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Game not found."));
    }

    @Test
    public void deleteUserGame_GameExists_ShouldDeleteGame() throws Exception {
        long gameId = 1L;

        when(userGameRepository.existsById(gameId)).thenReturn(true); // Simulate game with id in list

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

        UserGame existingUserGame = new UserGame(); // Rating and note before being updated
        existingUserGame.setRating(2);
        existingUserGame.setNote("Original note");

        when(userGameRepository.findById(id)).thenReturn(Optional.of(existingUserGame)); // When it searches by id, it returns existing user game

        mockMvc.perform(put("/user-games/{id}", id).contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Game updated."));

        ArgumentCaptor<UserGame> captor = ArgumentCaptor.forClass(UserGame.class); // Create captor
        verify(userGameRepository).save(captor.capture()); // When saving to database, capture the value passed in

        UserGame updatedUserGame = captor.getValue();
        assertEquals(5, updatedUserGame.getRating()); // Check that rating was updated
        assertEquals("Test note", updatedUserGame.getNote()); // Check that note was updated
    }

    @Test
    public void updateUserGame_GameDoesntExist_ShouldReturnNotFound() throws Exception {
        long id = 1L;
        request.setGameId(id);

        when(userGameRepository.findById(request.getGameId())).thenReturn(Optional.empty()); // Simulate game not in user's list

        mockMvc.perform(put("/user-games/{id}", id).contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

    }

    @Test
    public void updateUserGame_RatingNotValid_ShouldReturnBadRequest() throws Exception {
        long id = 1L;
        request.setGameId(id);
        request.setRating(6); // Validation requires rating of 1-5

        when(userGameRepository.findById(request.getGameId())).thenReturn(Optional.of(new UserGame())); // Game is on user's list

        mockMvc.perform(put("/user-games/{id}", id).contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid input:")));
    }

    @Test
    public void updateUserGame_NoteNotValid_ShouldReturnBadRequest() throws Exception {
        long id = 1L;
        String testNote = "A".repeat(256); // String larger than 255 characters

        request.setGameId(id);
        request.setNote(testNote); // Invalid note

        when(userGameRepository.findById(request.getGameId())).thenReturn(Optional.of(new UserGame())); // Game is in user's list

        mockMvc.perform(put("/user-games/{id}", id).contentType("application/json").content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid input:")));
    }

}
