package com.faris.gametracker.controller;

import com.faris.gametracker.dto.PageResponse;
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
        List<Game> pagedGames = Arrays.asList(games.get(0), games.get(1)); // New list with only first 2 games
        PageResponse<Game> pageResponse = new PageResponse<>(pagedGames, false, true); // Create PageResponse with no previous page, but a next page

        when(gameService.getGames(null, null, null, 0, 1)).thenReturn(pageResponse);
        when(gameService.getGamesInList()).thenReturn(userGameIds);

        mockMvc.perform(get("/games").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("games"))
                .andExpect(model().attributeExists("games"))
                .andExpect(model().attributeExists("userGameIds"))
                .andExpect(model().attribute("games", pagedGames))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("hasNext", true))
                .andExpect(model().attribute("hasPrevious", false));
    }

    @Test
    public void getAllGames_EmptyLists_ShouldStillReturnGamesView() throws Exception {
        PageResponse<Game> emptyPage = new PageResponse<>(Collections.emptyList(), false, false); // No games returned

        when(gameService.getGames(null, null, null, 0, 18)).thenReturn(emptyPage);
        when(gameService.getGamesInList()).thenReturn(Collections.emptySet());

        mockMvc.perform(get("/games"))
                .andExpect(status().isOk())
                .andExpect(view().name("games"))
                .andExpect(model().attribute("games", Collections.emptyList())) // Should be no list of games
                .andExpect(model().attribute("userGameIds", Collections.emptySet())) // No games in users game list
                .andExpect(model().attribute("hasNext", false))
                .andExpect(model().attribute("hasPrevious", false));
    }


    @Test
    public void getAllGames_InvalidPageParameter_ShouldRedirectToErrorView() throws Exception {
        // -1 Page shouldn't exist, 0-indexed paging
        when(gameService.getGames(null, null, null, -1, 18))
                .thenThrow(new IllegalArgumentException("Invalid page number"));

        mockMvc.perform(get("/games").param("page", "-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("error")) // GlobalExceptionHandler will redirect to error page
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage", "Oops! Something went wrong.")); // Error message set in exception handler
    }

}
