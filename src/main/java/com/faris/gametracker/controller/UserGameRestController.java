package com.faris.gametracker.controller;

import com.faris.gametracker.dto.UserGameResponse;
import com.faris.gametracker.model.UserGame;
import com.faris.gametracker.repository.UserGameRepository;
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

    @Autowired
    public UserGameRestController(UserGameRepository userGameRepository, UserGameService userGameService) {
        this.userGameRepository = userGameRepository;
        this.userGameService = userGameService;
    }

    /**
     * Returns all games in user list, filtered and sorted down by optional parameters.
     * Uses a DTO to filter the information being passed, also allows details from the Game model to be added
     *
     * @param searchQuery    Optional parameter that filters games that contain this in the title, developer or publisher
     * @param sortBy         Optional parameter that sorts values if received string is "rating", "releaseDate" or "title"
     * @param filterByRating Optional parameter that takes in an integer to filter games that have this rating
     * @return list of user games response
     */
    @GetMapping
    public List<UserGameResponse> getUserGames(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) Integer filterByRating) {

        List<UserGame> allUserGames = userGameRepository.findAll();
        allUserGames = userGameService.filterBySearch(allUserGames, searchQuery);
        allUserGames = userGameService.filterByRating(allUserGames, filterByRating);
        allUserGames = userGameService.sortUserGames(allUserGames, sortBy);

        return userGameService.convertToUserGameResponse(allUserGames);
    }
}
