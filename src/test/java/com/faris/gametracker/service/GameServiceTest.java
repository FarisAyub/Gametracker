package com.faris.gametracker.service;

import com.faris.gametracker.model.Game;
import com.faris.gametracker.model.UserGame;
import com.faris.gametracker.repository.GameRepository;
import com.faris.gametracker.repository.UserGameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private UserGameRepository userGameRepository;

    @InjectMocks
    private GameService gameService;

    private List<UserGame> userGames;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Setup games
        Game game1 = new Game("url1", "The Witcher 3", LocalDate.of(2001, 5, 5), "CD Projekt Red", "CDPR");
        game1.setId(1L);

        Game game2 = new Game("url2", "Elden Ring", LocalDate.of(2005, 2, 6), "FromSoftware", "FS");
        game2.setId(2L);

        // Setup user games
        userGames = Arrays.asList(
                new UserGame(game1, 5, "Great"),
                new UserGame(game2, 4, "Good")
        );
    }

    @Test
    public void getGamesInList_ShouldReturnSetOfGameIds() {
        when(userGameRepository.findAll()).thenReturn(userGames);

        Set<Long> result = gameService.getGamesInList();

        Set<Long> expected = new HashSet<>(Arrays.asList(1L, 2L));
        assertEquals(expected, result);
    }

    @Test
    public void getGamesInList_EmptyList_ShouldReturnEmptySet() {
        when(userGameRepository.findAll()).thenReturn(List.of());

        Set<Long> result = gameService.getGamesInList();

        assertEquals(0, result.size());
    }
}
