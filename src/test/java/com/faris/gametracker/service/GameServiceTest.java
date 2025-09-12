package com.faris.gametracker.service;

import com.faris.gametracker.dto.PageResponse;
import com.faris.gametracker.model.Game;
import com.faris.gametracker.model.UserGame;
import com.faris.gametracker.repository.GameRepository;
import com.faris.gametracker.repository.UserGameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

public class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private UserGameRepository userGameRepository;

    @Mock
    private FilterService filterService;

    @Mock
    private PaginationService paginationService;

    @Spy
    @InjectMocks
    private GameService gameService;

    private List<Game> games;
    private List<UserGame> userGames;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Setup games
        Game game1 = new Game("url1", "The Witcher 3", LocalDate.of(2001, 5, 5), "CD Projekt Red", "CDPR");
        game1.setId(1L);

        Game game2 = new Game("url2", "Elden Ring", LocalDate.of(2005, 2, 6), "FromSoftware", "FS");
        game2.setId(2L);

        // Set up a list of 2 games
        games = Arrays.asList(game1, game2);

        // Set up list of 2 user games
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


    @Test
    public void getGames_ShouldReturnPageOfGames() {
        // Mock filtering
        when(gameRepository.findAll()).thenReturn(games);
        when(gameService.getGamesInList()).thenReturn(Set.of(1L, 2L));
        when(filterService.filterGames(games, null, null, null, Set.of(1L, 2L))).thenReturn(games);

        // Mock pagination with no previous or next page
        PageResponse<Game> paged = new PageResponse<>(games, false, false);
        when(paginationService.paginate(games, 0, 10)).thenReturn(paged);

        PageResponse<Game> result = gameService.getGames(null, null, null, 0, 10);

        assertEquals(2, result.getPagedList().size()); // Should return 2 games
        assertEquals("The Witcher 3", result.getPagedList().get(0).getTitle()); // First game
        assertEquals("Elden Ring", result.getPagedList().get(1).getTitle()); // Second game
        assertFalse(result.isHasNext()); // Should have no previous page
        assertFalse(result.isHasPrevious()); // Should have no next page
    }
}
