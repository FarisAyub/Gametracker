package com.faris.gametracker.controller;

import com.faris.gametracker.model.Game;
import com.faris.gametracker.model.UserAccount;
import com.faris.gametracker.model.UserGame;
import com.faris.gametracker.repository.GameRepository;
import com.faris.gametracker.repository.UserAccountRepository;
import com.faris.gametracker.repository.UserGameRepository;
import com.faris.gametracker.service.GameApiService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/games")
public class GameController {

    private final GameRepository gameRepository;
    private final UserGameRepository userGameRepository;
    private final UserAccountRepository userAccountRepository;
    private final GameApiService gameApiService;

    public GameController(GameRepository gameRepository, UserGameRepository userGameRepository, GameApiService gameApiService, UserAccountRepository userAccountRepository) {
        this.gameRepository = gameRepository;
        this.userGameRepository = userGameRepository;
        this.gameApiService = gameApiService;
        this.userAccountRepository = userAccountRepository;
    }

    /**
     * Returns all games to the games view. Takes in optional parameters to modify the list being returned
     *
     * @param searchQuery Optional parameter which is used to filter the games list by the user entered string
     * @param sortBy      Optional parameter that provides a string used to decide what to sort by, "releaseDate" or "title"
     * @param showFilter  Optional parameter of either "inList" or "notInList" which filters which games to show
     * @param page        Parameter to filter to current page, starts at 0, once the user changes page, it's passed in as parameter
     * @param size        Parameter to filter the amount of games to display on each page, has a default value of 18, but can be modified by passing a different value
     * @param model       Used to return information back to the thymeleaf html page at the games page
     * @return Returns attributes back to the games page using the model, does not redirect the user
     */
    @GetMapping
    public String getAllGames(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam(required = false) String searchQuery,
                              @RequestParam(required = false) String sortBy,
                              @RequestParam(required = false) String showFilter,
                              @RequestParam(required = false, defaultValue = "0") Integer page,
                              @RequestParam(required = false, defaultValue = "18") Integer size,
                              Model model) {
        UserAccount user = userAccountRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));

        // Get all games and user games which are on the current user's list
        List<Game> games = gameRepository.findAll();
        List<UserGame> userGames = userGameRepository.findByUser(user);

        // Create a Set of ID's for every entry in userGames
        Set<Long> userGameIds = userGames.stream().map(userGame -> userGame.getGame().getId()).collect(Collectors.toSet());

        // Filter and sort
        games = filterBySearch(games, searchQuery);
        games = sortGames(games, sortBy);
        games = filterByInList(games, showFilter, userGameIds);

        // Create pointers for pagination
        int start = page * size; // Take current page multiplied by games per page to find the start index for this page
        int end = Math.min(start + size, games.size()); // Take the start index and add the size of page, using math.min in case there's not enough games for full page

        // List to contain our current page of games
        List<Game> pageOfGames;
        if (start >= games.size()) {
            // If the page is out of bounds, return an empty list
            pageOfGames = Collections.emptyList();
        } else {
            // Create a new list that's a sublist containing 1 page of games, using start and end index
            pageOfGames = games.subList(start, end);
        }

        // Booleans to be used by the page buttons, if there's no more games to left/right, disable button for moving page
        boolean hasNext = end < games.size();
        boolean hasPrevious = page > 0;

        // Values returned to the html, pass back the search query, sort type and list of games, these
        // are used to make sure the filter option selected doesn't reset, as we pass these back and then set the filters to what they were previously.
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("showFilter", showFilter);
        model.addAttribute("games", pageOfGames);
        model.addAttribute("userGameIds", userGameIds);
        model.addAttribute("currentPage", page);
        model.addAttribute("hasNext", hasNext);
        model.addAttribute("hasPrevious", hasPrevious);
        return "games";
    }

    /**
     * Takes a list of games, a string of "inList" or "notInList" and a set of Longs. It then creates a new list
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
     * Filters a list of Game objects based on the string passed in.
     *
     * @param games       the list of games to filter
     * @param searchQuery the string that was searched for
     * @return filtered list containing only games that contain the searchQuery as a substring in any of its fields
     */
    public List<Game> filterBySearch(List<Game> games, String searchQuery) {
        if (searchQuery == null || searchQuery.isEmpty()) return games; // Exit case if no search string was set
        String query = searchQuery.toLowerCase(); // Convert to lower case, so we can ignore capitalisation
        games = games.stream().filter(game ->
                game.getTitle().toLowerCase().contains(query) ||
                        game.getDeveloper().toLowerCase().contains(query) ||
                        game.getPublisher().toLowerCase().contains(query)).collect(Collectors.toList());
        return games;
    }

    /**
     * Sorts a list of Game objects based on the sorting string passed in.
     *
     * @param games  the list of games to sort
     * @param sortBy the sorting criteria which is "title" and "releaseDate"
     * @return a new sorted list, or the original list if no sort string was set
     */
    public List<Game> sortGames(List<Game> games, String sortBy) {
        List<Game> sorted = new ArrayList<>(games); // Prevents issues with an immutable list

        switch (sortBy != null ? sortBy : "") {
            case "title" -> sorted.sort(Comparator.comparing(Game::getTitle, String.CASE_INSENSITIVE_ORDER));
            case "releaseDate" -> sorted.sort(Comparator.comparing(Game::getReleaseDate));
        }
        return sorted;
    }
}
