package com.faris.gametracker.service;

import com.faris.gametracker.dto.UserGameResponse;
import com.faris.gametracker.model.UserGame;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserGameService {
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
     * Filters a list of UserGame objects based on the string passed in.
     *
     * @param games       the list of games to filter
     * @param searchQuery the string that was searched for
     * @return filtered list containing only games that contain the searchQuery as a substring in any of its fields
     */
    public List<UserGame> filterBySearch(List<UserGame> games, String searchQuery) {
        if (searchQuery == null || searchQuery.isEmpty()) return games; // Exit case if no search string was set
        String query = searchQuery.toLowerCase(); // Convert to lower case, so we can ignore capitalisation
        games = games.stream().filter(game -> game.getGame().getTitle().toLowerCase().contains(query) ||
                game.getGame().getDeveloper().toLowerCase().contains(query) ||
                game.getGame().getPublisher().toLowerCase().contains(query)).collect(Collectors.toList());
        return games;
    }

    /**
     * Filters a list of UserGame objects based on the rating passed in.
     *
     * @param games  the list of games to filter
     * @param rating the rating number to filter by values can be 5,4,3,2 and 1
     * @return filtered list, or the original list if no rating was set
     */
    public List<UserGame> filterByRating(List<UserGame> games, Integer rating) {
        if (rating == null || rating == -1 || rating > 5)
            return games; // Exit case if rating was not set (we pass -1 value for the "no rating" option

        games = games.stream().filter(game -> game.getRating() == rating).collect(Collectors.toList());
        return games;
    }

    /**
     * Sorts a list of UserGame objects based on the sorting string passed in.
     *
     * @param games  the list of games to sort
     * @param sortBy the sorting criteria which is "title", "releaseDate", and "rating"
     * @return a new sorted list, or the original list if no sort string was set
     */
    public List<UserGame> sortUserGames(List<UserGame> games, String sortBy) {
        List<UserGame> sorted = new ArrayList<>(games); // Prevents issues with an immutable list

        switch (sortBy != null ? sortBy : "") {
            case "title" ->
                    sorted.sort(Comparator.comparing(g -> g.getGame().getTitle(), String.CASE_INSENSITIVE_ORDER));
            case "releaseDate" -> sorted.sort(Comparator.comparing(g -> g.getGame().getReleaseDate()));
            case "rating" -> sorted.sort(Comparator.comparing(UserGame::getRating).reversed());
        }
        return sorted;
    }
}
