package com.faris.gametracker.service;

import com.faris.gametracker.dto.PageResponse;
import com.faris.gametracker.dto.UserGameRequest;
import com.faris.gametracker.dto.UserGameResponse;
import com.faris.gametracker.model.Game;
import com.faris.gametracker.model.UserGame;
import com.faris.gametracker.repository.GameRepository;
import com.faris.gametracker.repository.UserGameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserGameServiceTest {

    private UserGameRepository userGameRepository;
    private GameRepository gameRepository;
    private UserGameService userGameService;
    private FilterService filterService;
    private PaginationService paginationService;

    private Game game;
    private UserGameRequest request;
    private UserGame existingUserGame;
    private List<UserGame> userGames;

    @BeforeEach
    public void setup() {
        userGameRepository = mock(UserGameRepository.class);
        gameRepository = mock(GameRepository.class);
        filterService = mock(FilterService.class);
        paginationService = mock(PaginationService.class);

        userGameService = new UserGameService(userGameRepository, gameRepository, filterService, paginationService);

        // Single game
        game = new Game("url", "The Witcher 3", LocalDate.of(2001, 5, 5), "CD Projekt Red", "CDPR");
        game.setId(1L);

        // UserGameRequest
        request = new UserGameRequest();
        request.setGameId(1L);
        request.setRating(5);
        request.setNote("Great!");

        // Existing UserGame
        existingUserGame = new UserGame(game, 5, "Old note");
        existingUserGame.setId(1L);

        // Create a UserGames list with 2 entries
        UserGame userGame1 = new UserGame(game, 5, "Great!");
        userGame1.setId(10L);
        UserGame userGame2 = new UserGame(game, 3, "Okay");
        userGame2.setId(11L);
        userGames = Arrays.asList(userGame1, userGame2);
    }

    @Test
    public void getUserGameResponse_ShouldReturnPagedResults_WithHasNextAndHasPrevious() {
        // Contains 2 games
        when(userGameRepository.findAll()).thenReturn(userGames);
        when(filterService.filterUserGames(userGames, null, null, null)).thenReturn(userGames);

        // Mock data
        PageResponse<UserGame> paged = new PageResponse<>(List.of(userGames.get(0)), false, true);
        when(paginationService.paginate(userGames, 0, 1)).thenReturn(paged);

        // Get response with no filters, page is 0 and size is 1
        PageResponse<UserGameResponse> result = userGameService.getUserGameResponse(null, null, null, 0, 1);

        assertEquals(1, result.getPagedList().size()); // Should return 1 as page size is 1
        assertEquals(10L, result.getPagedList().get(0).getId()); // Make sure the id for first element is correct
        assertTrue(result.isHasNext()); // Should be a second page
        assertFalse(result.isHasPrevious()); // Page 0 shouldn't be any previous pages
    }

    @Test
    public void getUserGameResponse_ShouldSetHasPreviousTrue() {
        // Contains 2 games
        when(userGameRepository.findAll()).thenReturn(userGames);
        when(filterService.filterUserGames(userGames, null, null, null)).thenReturn(userGames);

        // Mock data
        PageResponse<UserGame> paged = new PageResponse<>(List.of(userGames.get(1)), true, false);
        when(paginationService.paginate(userGames, 1, 1)).thenReturn(paged);

        // Get response with no filters, page is 1, size of page is 1 (page is 0-indexed)
        PageResponse<UserGameResponse> result = userGameService.getUserGameResponse(null, null, null, 1, 1);

        assertEquals(1, result.getPagedList().size()); // Should have 1 result on this page
        assertEquals(11L, result.getPagedList().get(0).getId()); // Make sure the id for first element is correct
        assertFalse(result.isHasNext()); // Only 2 games, so should be no next page
        assertTrue(result.isHasPrevious()); // On page 2, so should be a previous page
    }

    @Test
    public void getUserGameResponse_ShouldReturnEmptyList_WhenPageOutOfRange() {
        // Contains 2 games
        when(userGameRepository.findAll()).thenReturn(userGames);
        when(filterService.filterUserGames(userGames, null, null, null)).thenReturn(userGames);

        // Mock data
        PageResponse<UserGame> paged = new PageResponse<>(List.of(), true, false);
        when(paginationService.paginate(userGames, 5, 2)).thenReturn(paged);

        // Get response with no filters, page is 5, size of page is 2
        PageResponse<UserGameResponse> result = userGameService.getUserGameResponse(null, null, null, 5, 2);

        assertTrue(result.getPagedList().isEmpty()); // Should be no games for this page
        assertFalse(result.isHasNext()); // Shouldn't have a next page since out of bounds
        assertTrue(result.isHasPrevious()); // Should have a previous page since page isn't 0
    }

    @Test
    public void convertToUserGameResponse_ShouldReturnCorrectDTOList() {
        List<UserGameResponse> response = userGameService.convertToUserGameResponse(userGames);

        assertEquals(2, response.size());
        assertEquals(10L, response.get(0).getId());
        assertEquals("The Witcher 3", response.get(0).getTitle());
        assertEquals(5, response.get(0).getRating());
    }

    @Test
    public void addUserGame_ShouldSaveGame_WhenNotAlreadyInList() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(userGameRepository.existsByGameId(1L)).thenReturn(false);

        userGameService.addUserGame(request);

        ArgumentCaptor<UserGame> captor = ArgumentCaptor.forClass(UserGame.class);
        verify(userGameRepository).save(captor.capture());

        UserGame saved = captor.getValue();
        assertEquals(5, saved.getRating());
        assertEquals("Great!", saved.getNote());
        assertEquals(game, saved.getGame());
    }

    @Test
    public void addUserGame_GameNotFound_ShouldThrowException() {
        when(gameRepository.findById(999L)).thenReturn(Optional.empty());
        request.setGameId(999L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userGameService.addUserGame(request));
        assertEquals("Game not found.", ex.getMessage());
    }

    @Test
    public void addUserGame_GameAlreadyInList_ShouldThrowException() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));
        when(userGameRepository.existsByGameId(1L)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> userGameService.addUserGame(request));
        assertEquals("Game is already in your list.", ex.getMessage());
    }

    @Test
    public void updateUserGame_ShouldUpdateExistingUserGame() {
        when(userGameRepository.findById(1L)).thenReturn(Optional.of(existingUserGame));

        UserGameRequest updatedRequest = new UserGameRequest();
        updatedRequest.setRating(4);
        updatedRequest.setNote("Updated note");

        userGameService.updateUserGame(updatedRequest, 1L);

        assertEquals(4, existingUserGame.getRating());
        assertEquals("Updated note", existingUserGame.getNote());
        verify(userGameRepository).save(existingUserGame);
    }

    @Test
    public void updateUserGame_GameNotInList_ShouldThrowException() {
        when(userGameRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userGameService.updateUserGame(request, 1L));
        assertEquals("Game is not in your list.", ex.getMessage());
    }

    @Test
    public void deleteUserGame_ShouldDelete_WhenGameExists() {
        when(userGameRepository.existsById(1L)).thenReturn(true);

        userGameService.deleteUserGame(1L);

        verify(userGameRepository).deleteById(1L);
    }

    @Test
    public void deleteUserGame_GameNotInList_ShouldThrowException() {
        when(userGameRepository.existsById(1L)).thenReturn(false);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> userGameService.deleteUserGame(1L));
        assertEquals("Game is not in your list.", ex.getMessage());
    }
}
