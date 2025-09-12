package com.faris.gametracker.service;

import com.faris.gametracker.dto.PagedUserGameResponse;
import com.faris.gametracker.dto.UserGameRequest;
import com.faris.gametracker.dto.UserGameResponse;
import com.faris.gametracker.model.Game;
import com.faris.gametracker.model.UserGame;
import com.faris.gametracker.repository.GameRepository;
import com.faris.gametracker.repository.UserGameRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class UserGameService {

    private final UserGameRepository userGameRepository;
    private final GameRepository gameRepository;
    private final FilterService filterService;

    public UserGameService(UserGameRepository userGameRepository, GameRepository gameRepository, FilterService filterService) {
        this.userGameRepository = userGameRepository;
        this.gameRepository = gameRepository;
        this.filterService = filterService;
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

    public PagedUserGameResponse getUserGameResponse(String filterSearch, String filterSort, Integer filterRating, Integer page, Integer size) {
        // Filter all games by passed filters
        List<UserGame> filtered = filterService.filterUserGames(userGameRepository.findAll(), filterSearch, filterSort, filterRating);


        // Create pointers for pagination
        int start = page * size; // Take current page multiplied by games per page to find the start index for this page
        int end = Math.min(start + size, filtered.size()); // Set end index to start index + amount per page, returning lower if there's not enough left in list

        List<UserGame> pageOfGames;
        if (start >= filtered.size()) {
            pageOfGames = Collections.emptyList();
        } else {
            pageOfGames = filtered.subList(start, end);
        }

        // Booleans to be used by the page buttons, if there's no more games to left/right, disable button for moving page
        boolean hasNext = end < filtered.size();
        boolean hasPrevious = page > 0;

        return new PagedUserGameResponse(convertToUserGameResponse(pageOfGames), hasPrevious, hasNext);
    }
}
