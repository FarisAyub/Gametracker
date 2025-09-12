package com.faris.gametracker.service;

import com.faris.gametracker.dto.PageResponse;
import com.faris.gametracker.dto.UserGameRequest;
import com.faris.gametracker.dto.UserGameResponse;
import com.faris.gametracker.model.Game;
import com.faris.gametracker.model.UserGame;
import com.faris.gametracker.repository.GameRepository;
import com.faris.gametracker.repository.UserGameRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserGameService {

    private final UserGameRepository userGameRepository;
    private final GameRepository gameRepository;
    private final FilterService filterService;
    private final PaginationService paginationService;

    public UserGameService(UserGameRepository userGameRepository, GameRepository gameRepository, FilterService filterService, PaginationService paginationService) {
        this.userGameRepository = userGameRepository;
        this.gameRepository = gameRepository;
        this.filterService = filterService;
        this.paginationService = paginationService;
    }

    /**
     * Converts a list of user games into a list of user game response, which contains fields for both the game details
     * and the user's game details like rating and note
     *
     * @param allUserGames A list of UserGames to be converted into dto UserGameResponse
     * @return List of UserGameResponse which contains fields related to both the game and user game
     */
    public List<UserGameResponse> convertToUserGameResponse(List<UserGame> allUserGames) {
        List<UserGameResponse> list = new ArrayList<>(); // List using dto to store each game with additional info

        for (UserGame userGame : allUserGames) {
            UserGameResponse userGameResponse = new UserGameResponse(
                    userGame.getId(),
                    userGame.getGame().getUrl(),
                    userGame.getGame().getTitle(),
                    userGame.getGame().getDeveloper(),
                    userGame.getGame().getPublisher(),
                    userGame.getGame().getReleaseDate(),
                    userGame.getRating(),
                    userGame.getNote()
            );
            list.add(userGameResponse); // Add new each UserGameResponse to the list
        }
        return list;
    }

    /**
     * Adds a game to the user's list, storing it in the User Game database
     *
     * @param request Contains the details of the game and user game that needs to be added
     */
    public void addUserGame(UserGameRequest request) {
        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new IllegalArgumentException("Game not found.")); // Optional as the gameId passed in may not exist

        if (userGameRepository.existsByGameId(request.getGameId())) { // Check if the game is already in the users list, if so don't add
            throw new IllegalStateException("Game is already in your list.");
        }

        UserGame userGame = new UserGame(); // Create a new user game and assign the values to it
        userGame.setGame(game); // Set the game
        userGame.setRating(request.getRating()); // Set the rating
        userGame.setNote(request.getNote()); // Set the note
        userGameRepository.save(userGame); // Add to the database
    }

    /**
     * Updates a game in the user's list, applying the updated rating and note and saving it to the database
     *
     * @param request Contains the updated user rating and note
     * @param id ID of the current User Game entry
     */
    public void updateUserGame(UserGameRequest request, Long id) {
        UserGame userGame = userGameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Game is not in your list."));

        userGame.setRating(request.getRating());
        userGame.setNote(request.getNote());
        userGameRepository.save(userGame);
    }

    /**
     * Deletes a game from the user's list
     *
     * @param gameId ID of the game that will be removed from the user's list
     */
    public void deleteUserGame(Long gameId) {
        if (!userGameRepository.existsById(gameId)) {
            throw new IllegalStateException("Game is not in your list.");
        }

        userGameRepository.deleteById(gameId);
    }

    /**
     * Returns page of User Games that have been filtered
     *
     * @param filterSearch Search String to filter by
     * @param filterSort Sorting option for ordering
     * @param filterRating Rating to filter by
     * @param page Current page number
     * @param size Size of each page
     * @return PageResponse which contains List of UserGameResponse DTO's, and whether there's a next and previous page
     */
    public PageResponse<UserGameResponse> getUserGameResponse(String filterSearch, String filterSort, Integer filterRating, Integer page, Integer size) {
        // Filter UserGames
        List<UserGame> filtered = filterService.filterUserGames(userGameRepository.findAll(), filterSearch, filterSort, filterRating);

        // Paginate the filtered list
        PageResponse<UserGame> paged = paginationService.paginate(filtered, page, size);

        // Convert PageResponse's list to UserGameResponse DTO instead of just UserGame
        return new PageResponse<>(convertToUserGameResponse(paged.getPagedList()), paged.isHasPrevious(), paged.isHasNext());
    }
}
