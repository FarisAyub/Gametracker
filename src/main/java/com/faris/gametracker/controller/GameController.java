package com.faris.gametracker.controller;

import com.faris.gametracker.model.Game;
import com.faris.gametracker.model.UserGame;
import com.faris.gametracker.repository.GameRepository;
import com.faris.gametracker.repository.UserGameRepository;
import com.faris.gametracker.service.FilterService;
import com.faris.gametracker.service.GameApiService;
import com.faris.gametracker.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final GameApiService gameApiService;
    private final FilterService filterService;
    private final GameService gameService;

    public GameController(GameRepository gameRepository, UserGameRepository userGameRepository, GameApiService gameApiService, FilterService filterService, GameService gameService) {
        this.gameRepository = gameRepository;
        this.userGameRepository = userGameRepository;
        this.gameApiService = gameApiService;
        this.filterService = filterService;
        this.gameService = gameService;
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
    public String getAllGames(@RequestParam(required = false) String searchQuery,
                              @RequestParam(required = false) String sortBy,
                              @RequestParam(required = false) String showFilter,
                              @RequestParam(required = false, defaultValue = "0") Integer page,
                              @RequestParam(required = false, defaultValue = "18") Integer size,
                              Model model) {

        // Get all games and user games
        List<Game> games = gameRepository.findAll();

        // Create a Set of ID's for every entry in userGames
        Set<Long> userGameIds = gameService.getGamesInList();

        // Filter and sort
        games = filterService.filterGames(games, searchQuery, sortBy, showFilter, userGameIds);

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

}
