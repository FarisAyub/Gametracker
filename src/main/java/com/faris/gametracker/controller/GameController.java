package com.faris.gametracker.controller;

import com.faris.gametracker.dto.PageResponse;
import com.faris.gametracker.model.Game;
import com.faris.gametracker.repository.GameRepository;
import com.faris.gametracker.repository.UserGameRepository;
import com.faris.gametracker.service.FilterService;
import com.faris.gametracker.service.GameApiService;
import com.faris.gametracker.service.GameService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
     * @param filterSearch Optional parameter which is used to filter the games list by the user entered string
     * @param filterSort      Optional parameter that provides a string used to decide what to sort by, "releaseDate" or "title"
     * @param filterList  Optional parameter of either "inList" or "notInList" which filters which games to show
     * @param page        Parameter to filter to current page, starts at 0, once the user changes page, it's passed in as parameter
     * @param size        Parameter to filter the amount of games to display on each page, has a default value of 18, but can be modified by passing a different value
     * @param model       Used to return information back to the thymeleaf html page at the games page
     * @return Returns attributes back to the games page using the model, does not redirect the user
     */
    @GetMapping
    public String getAllGames(@RequestParam(required = false) String filterSearch,
                              @RequestParam(required = false) String filterSort,
                              @RequestParam(required = false) String filterList,
                              @RequestParam(required = false, defaultValue = "0") Integer page,
                              @RequestParam(required = false, defaultValue = "18") Integer size,
                              Model model) {

        // Gets one page of filtered results
        PageResponse<Game> pageOfGames = gameService.getGames(filterSearch, filterSort, filterList, page, size);

        model.addAttribute("filterSearch", filterSearch);
        model.addAttribute("filterSort", filterSort);
        model.addAttribute("filterList", filterList);
        model.addAttribute("games", pageOfGames.getPagedList());
        model.addAttribute("userGameIds", gameService.getGamesInList());
        model.addAttribute("currentPage", page);
        model.addAttribute("hasNext", pageOfGames.isHasNext());
        model.addAttribute("hasPrevious", pageOfGames.isHasPrevious());
        return "games";
    }

}
