package com.faris.gametracker.service;

import com.faris.gametracker.model.Game;
import com.faris.gametracker.model.GameView;
import com.faris.gametracker.model.UserGame;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilterService {

    // GENERIC FILTERS
    /**
     * Filters a list of Game or UserGame objects based on the string passed in.
     *
     * @param games       the list of Games/UserGames to filter
     * @param searchQuery the search string
     * @return filtered list containing only games that contain the searchQuery as a substring in any of its fields
     */
    public <T extends GameView> List<T> filterBySearch(List<T> games, String searchQuery) {
        if (searchQuery == null || searchQuery.isEmpty()) return games; // Exit case if no search string was set
        String query = searchQuery.toLowerCase(); // Convert to lower case, so we can ignore capitalisation

        games = games.stream().filter(game ->
                        game.getTitle().toLowerCase().contains(query) ||
                        game.getDeveloper().toLowerCase().contains(query) ||
                        game.getPublisher().toLowerCase().contains(query))
                            .collect(Collectors.toList());
        return games;
    }

    /**
     * Sorts a list of Game or UserGame objects based on the sorting string passed in.
     *
     * @param games  List of generics, representing the list of Games or UserGames to sort
     * @param sortBy The sorting criteria which is "title", "releaseDate" or "rating"
     * @return A new sorted list, or the original list if sort string didn't match any sort options
     */
    public <T extends GameView> List<T> filterSort(List<T> games, String sortBy) {
        if (games == null || games.isEmpty()) return games;

        List<T> sorted = new ArrayList<>(games);

        switch (sortBy != null ? sortBy : "") {
            case "title" ->
                    sorted.sort(Comparator.comparing(GameView::getTitle, String.CASE_INSENSITIVE_ORDER));
            case "releaseDate" ->
                    sorted.sort(Comparator.comparing(GameView::getReleaseDate));
            case "rating" ->
                    sorted.sort(Comparator.comparing(GameView::getRating, Comparator.nullsLast(Comparator.reverseOrder())));
        }

        return sorted;
    }

    // GAME FILTERS
    /**
     * Takes a list of games, a string of "inList" or "notInList" and a set of Longs. Creates a new list
     * filtered by comparing the id of the games in the list to the id's in the set, returning a new list
     * containing only the games in the list or not in the list
     *
     * @param games       List of games to apply filter to
     * @param showFilter  String that is either "inList" or "notInList" for method of filtering
     * @param userGameIds A Set of Long's that corresponds to game id's that are in the user's game list
     * @return A filtered list containing only the games that are in list or not (returns original list if invalid string for showFilter)
     */
    public List<Game> filterByInList(List<Game> games, String showFilter, Set<Long> userGameIds) {
        if (showFilter == null || showFilter.isEmpty()) return games;

        return switch (showFilter) { // Checks if each game's id exists in the set of userGameId's, and returns a new list filtered
            case "inList" ->
                    games.stream().filter(game -> userGameIds.contains(game.getId())).collect(Collectors.toList());
            case "notInList" ->
                    games.stream().filter(game -> !userGameIds.contains(game.getId())).collect(Collectors.toList());
            default -> games;
        };
    }

    /**
     *  Combines Search and filter options into one, takes in the list of games and applies all 3
     *  filter and sort types before returning the new list
     *
     * @param games List of games to be filtered
     * @param searchQuery Search String to filter by
     * @param sortBy Sorting option for ordering
     * @param showFilter Whether to show games in user's list or not
     * @param userGameIds List of games in the user's list, for filtering them in our out
     * @return List that has been filtered and sorted depending on options passed in
     */
    public List<Game> filterGames(List<Game> games, String searchQuery, String sortBy, String showFilter, Set<Long> userGameIds) {
        games = filterBySearch(games, searchQuery);
        games = filterSort(games, sortBy);
        games = filterByInList(games, showFilter, userGameIds);
        return games;
    }

    // USER-GAME FILTERS
    /**
     * Filters a list of UserGame objects based on the rating passed in
     *
     * @param games  The list of games to filter
     * @param rating The rating number to filter by values can be 5,4,3,2 or 1
     * @return Filtered list, or the original list if no rating was set
     */
    public List<UserGame> filterByRating(List<UserGame> games, Integer rating) {
        if (rating == null || rating == -1 || rating > 5)
            return games; // Exit case if rating was not set (we pass -1 value for the "no rating" option)

        games = games.stream().filter(game -> Objects.equals(game.getRating(), rating)).collect(Collectors.toList());
        return games;
    }

    /**
     * Combines search and filter options for UserGame into one, returning a new list with all 3 filter options applied
     *
     * @param userGames List of UserGames to be filtered
     * @param searchQuery Search String to filter by
     * @param sortBy Sorting option for ordering
     * @param filterByRating Rating to be filtered by, only rating passed in will be in returned list
     * @return List that has been filtered and sorted using passed in parameters
     */
    public List<UserGame> filterUserGames(List<UserGame> userGames, String searchQuery, String sortBy, Integer filterByRating) {
        userGames = filterBySearch(userGames, searchQuery);
        userGames = filterByRating(userGames, filterByRating);
        userGames = filterSort(userGames, sortBy);
        return userGames;
    }

}
