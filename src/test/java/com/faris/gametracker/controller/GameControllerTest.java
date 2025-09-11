package com.faris.gametracker.controller;

import com.faris.gametracker.model.Game;
import com.faris.gametracker.repository.GameRepository;
import com.faris.gametracker.repository.UserGameRepository;
import com.faris.gametracker.service.FilterService;
import com.faris.gametracker.service.GameApiService;
import com.faris.gametracker.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;

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

    @MockBean
    private FilterService filterService;

    @MockBean
    private GameService gameService;

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
        when(gameRepository.findAll()).thenReturn(games);
        when(userGameRepository.findAll()).thenReturn(Collections.emptyList());

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

}
