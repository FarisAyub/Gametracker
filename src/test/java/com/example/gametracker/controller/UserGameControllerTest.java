package com.example.gametracker.controller;

import com.example.gametracker.model.Game;
import com.example.gametracker.model.UserGame;
import com.example.gametracker.repository.GameRepository;
import com.example.gametracker.repository.UserGameRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.mockito.Mockito.when;

// MockMVC testing
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserGameController.class)
public class UserGameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private UserGameRepository userGameRepository;

    @InjectMocks
    private UserGameController userGameController;

    @Test
    public void getAllUserGamesTest() throws Exception {
        Game game = new Game("https://media.rawg.io/media/games/618/618c2031a07bbff6b4f611f10b6bcdbc.jpg", "The Witcher 3", LocalDate.of(2001, Month.JANUARY, 15), "CD Projekt Red","CD Projekt Red");
        UserGame userGame = new UserGame(game, 5, "Great!");

        when(userGameRepository.findAll()).thenReturn(List.of(userGame));

        mockMvc.perform(get("/user-games")).andExpect(status().isOk());
        mockMvc.perform(get("/user-games")).andExpect(model().attributeExists("userGames"));
        mockMvc.perform(get("/user-games")).andExpect(model().attributeExists("searchQuery"));
        mockMvc.perform(get("/user-games")).andExpect(model().attributeExists("sortBy"));
        mockMvc.perform(get("/user-games")).andExpect(model().attributeExists("filterByRating"));
        mockMvc.perform(get("/user-games")).andExpect(model().attribute("currentPage", 0));
        mockMvc.perform(get("/user-games")).andExpect(model().attribute("hasNext", false));
        mockMvc.perform(get("/user-games")).andExpect(model().attribute("hasPrevious", false));

    }

}
