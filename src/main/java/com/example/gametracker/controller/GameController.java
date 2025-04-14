package com.example.gametracker.controller;

import com.example.gametracker.model.Game;
import com.example.gametracker.model.UserGame;
import com.example.gametracker.repository.GameRepository;
import com.example.gametracker.repository.UserGameRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/games")
public class GameController {

    private final GameRepository gameRepository;
    private final UserGameRepository userGameRepository;

    public GameController(GameRepository gameRepository , UserGameRepository userGameRepository) {
        this.gameRepository = gameRepository;
        this.userGameRepository = userGameRepository;
    }

    @GetMapping
    public String getAllGames(@RequestParam(required = false) String searchQuery,
                              @RequestParam(required = false) String sortBy,
                              @RequestParam(required = false) String showFilter,
                              @RequestParam(required = false, defaultValue = "0") Integer page,
                              @RequestParam(required = false, defaultValue = "18") Integer size,
                              Model model) {

        List<Game> games = gameRepository.findAll();
        List<UserGame> userGames = userGameRepository.findAll();

        // Create a set of Game id's that contains all the game id's on the users list, used to tag games already on the users list
        Set<Long> userGameIds = userGames.stream().map(userGame -> userGame.getGame().getId()).collect(Collectors.toSet());

        games = filterBySearch(games, searchQuery);
        games = sortGames(games, sortBy);
        games = filterByInList(games, showFilter, userGameIds);

        // Filter the list into pages
        int start = page * size; // Take current page and multiply by amount of games per page to find the start index for next page
        int end = Math.min(start + size, games.size()); // Take the start index and add the size of page, using math.min in case there's not enough games for full page

        // Create a new list that's a sublist of games showing the amount of games per page requested
        List<Game> pagedGames = games.subList(start, end);

        // Booleans to be used by the page buttons, if there's no more games to left/right, disable button for moving page
        boolean hasNext = end < games.size();
        boolean hasPrevious = page > 0;

        // Values returned to the html, pass back the search query, sort type and list of games, these
        // are used to make sure the filter option selected doesn't reset, as we pass these back and then set the filters to what they were previously.
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("showFilter", showFilter);
        model.addAttribute("games",pagedGames);
        model.addAttribute("userGameIds", userGameIds);
        model.addAttribute("currentPage", page);
        model.addAttribute("hasNext", hasNext);
        model.addAttribute("hasPrevious", hasPrevious);
        return "games";
    }

    private List<Game> filterByInList(List<Game> games, String showFilter, Set<Long> userGameIds) {
        if (showFilter == null || showFilter.isEmpty()) return games;
        List<Game> inList = new ArrayList<>();
        List<Game> notInList = new ArrayList<>();
        for (Game game : games) {
            if (userGameIds.contains(game.getId())) {
                inList.add(game);
            } else {
                notInList.add(game);
            }
        }
        if (showFilter.equals("inList")) {
            return inList;
        } else if (showFilter.equals("notInList")) {
            return notInList;
        }
        return games;
    }

    /**
     * Filters a list of Game objects based on the string passed in.
     *
     * @param games  the list of games to filter
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
