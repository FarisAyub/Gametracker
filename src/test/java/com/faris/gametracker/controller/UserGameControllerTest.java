package com.faris.gametracker.controller;

import com.faris.gametracker.dto.UserGameRequest;
import com.faris.gametracker.model.Game;
import com.faris.gametracker.model.UserGame;
import com.faris.gametracker.repository.GameRepository;
import com.faris.gametracker.repository.UserGameRepository;
import com.faris.gametracker.service.FilterService;
import com.faris.gametracker.service.GameApiService;
import com.faris.gametracker.service.UserGameService;
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

    @MockBean
    private FilterService filterService;

    @MockBean
    private UserGameService userGameService;

    @InjectMocks
    private UserGameController userGameController;

    private UserGameRequest validRequest;
    private UserGameRequest invalidRequest;

    private List<UserGame> userGames;

    @Autowired
    private ObjectMapper objectMapper; // To convert java to JSON for http requests like post

    @BeforeEach
    public void setup() {
        validRequest = new UserGameRequest();
        validRequest.setGameId(1L);
        validRequest.setRating(5);
        validRequest.setNote("Test note");

        invalidRequest = new UserGameRequest();
        invalidRequest.setGameId(1L);
        invalidRequest.setRating(6); // invalid rating
        invalidRequest.setNote("A".repeat(256)); // invalid note
    }

    @Test
    public void getAllUserGames_ShouldReturnOk() throws Exception {
        List<UserGame> userGames = Arrays.asList(new UserGame(), new UserGame());
        mockMvc.perform(get("/user-games"))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteUserGame_EndpointExists_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/user-games/{id}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    public void updateUserGame_InvalidInput_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/user-games/{id}", 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid input:")));
    }

    @Test
    public void addUserGame_InvalidInput_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/user-games")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Validation error: must be less than or equal to 5")));
    }
}
