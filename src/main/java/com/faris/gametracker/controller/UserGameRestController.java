package com.faris.gametracker.controller;

import com.faris.gametracker.dto.UserGameResponse;
import com.faris.gametracker.model.UserGame;
import com.faris.gametracker.repository.UserGameRepository;
import com.faris.gametracker.service.FilterService;
import com.faris.gametracker.service.UserGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user-games")
public class UserGameRestController {

    private final UserGameRepository userGameRepository;
    private final UserGameService userGameService;
    private final FilterService filterService;

    @Autowired
    public UserGameRestController(UserGameRepository userGameRepository, UserGameService userGameService, FilterService filterService) {
        this.userGameRepository = userGameRepository;
        this.userGameService = userGameService;
        this.filterService = filterService;
    }

    /**
     * Returns all games in user list, filtered and sorted down by optional parameters.
     * Uses a DTO to filter the information being passed, also allows details from the Game model to be added
     *
     * @param filterSearch    Optional parameter that filters games that contain this in the title, developer or publisher
     * @param filterSort         Optional parameter that sorts values if received string is "rating", "releaseDate" or "title"
     * @param filterRating Optional parameter that takes in an integer to filter games that have this rating
     * @return list of user games response
     */
    @GetMapping
    public List<UserGameResponse> getUserGames(
            @RequestParam(required = false) String filterSearch,
            @RequestParam(required = false) String filterSort,
            @RequestParam(required = false) Integer filterRating) {

        // Get all from database
        List<UserGame> allUserGames = userGameRepository.findAll();

        // Filter and sort the list
        allUserGames = filterService.filterUserGames(allUserGames, filterSearch, filterSort, filterRating);

        return userGameService.convertToUserGameResponse(allUserGames);
    }
}
