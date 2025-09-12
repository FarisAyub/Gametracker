package com.faris.gametracker.controller;

import com.faris.gametracker.dto.PageResponse;
import com.faris.gametracker.dto.UserGameRequest;
import com.faris.gametracker.dto.UserGameResponse;
import com.faris.gametracker.repository.GameRepository;
import com.faris.gametracker.repository.UserGameRepository;
import com.faris.gametracker.service.FilterService;
import com.faris.gametracker.service.UserGameService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/user-games")
public class UserGameController {

    private final UserGameService userGameService;

    @Autowired
    public UserGameController(UserGameService userGameService) {
        this.userGameService = userGameService;
    }

    /**
     * Lists all games in user list, filtered and sorted down by optional parameters.
     * Uses a DTO to filter the information being passed, also allows details from the Game model to be added
     *
     * @param filterSearch    Optional parameter that can be passed in by searching on the website, text is filtered by this
     * @param filterSort         Optional parameter that accepts values "rating" "releaseDate" and "title" and sorts alphabetically based on the parameter
     * @param filterRating Optional parameter that takes in an integer from -1 to 5 to be used to filter games that have this rating
     * @param page           Optional parameter that takes in the current page on user-games. Defaults to 0 for when a page hasn't been passed in
     * @param size           Optional parameter that takes a size for pages. Defaults to 9 user games per page unless modified
     * @param model          Used to return information back to the thymeleaf html page
     * @return the model and list of games and other variables back to the user-games page
     */
    @GetMapping
    public String getAllUserGames(
            @RequestParam(required = false) String filterSearch,
            @RequestParam(required = false) String filterSort,
            @RequestParam(required = false) Integer filterRating,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "9") Integer size,
            Model model) {

        // Gets one page of UserGames, which have been converted to DTO UserGameResponse
        PageResponse<UserGameResponse> pageOfGames = userGameService.getUserGameResponse(filterSearch, filterSort, filterRating, page, size);

        // Returns everything back to the template in thymeleaf
        model.addAttribute("userGames", pageOfGames.getPagedList());
        model.addAttribute("filterSearch", filterSearch);
        model.addAttribute("filterSort", filterSort);
        model.addAttribute("filterRating", filterRating);
        model.addAttribute("currentPage", page);
        model.addAttribute("hasNext", pageOfGames.isHasNext());
        model.addAttribute("hasPrevious", pageOfGames.isHasPrevious());
        return "user-games";
    }

    /**
     * Adds a game to the users games list, setting the rating and note provided
     *
     * @param request takes the post sent in as json to parse
     * @param result  gets populated if data validation for request fails
     * @return returns ResponseEntity, which will be displayed in toast containing success or error message
     */
    @PostMapping
    @ResponseBody
    public ResponseEntity<String> addUserGame(@RequestBody @Valid UserGameRequest request, BindingResult result) {
        if (result.hasErrors()) { // If data validation fails (rating is not 1-5 or note is longer than 255)
            String errorMessage = result.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body("Validation error: " + errorMessage);
        }

        // Call the addUserGame function, if it returns an exception, send ResponseEntity to be displayed in Toast
        try {
            userGameService.addUserGame(request);
            return ResponseEntity.ok("Game added to your list!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * Takes in an id from the URL and updates the User Game entry with the id passed. Uses the JSON data passed in
     * to overwrite the old values for the corresponding id
     *
     * @param id      The id from the url corresponding to the entry that needs to be updated
     * @param request The JSON body containing the updated rating and note
     * @param result  A binding result that will be populated if the request passed in doesn't meet the data validation rules (rating 1-5 and note max 255 chars)
     * @return returns ResponseEntity, which will be displayed in toast
     */
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<String> updateUserGame(@PathVariable Long id, @RequestBody @Valid UserGameRequest request, BindingResult result) {
        if (result.hasErrors()) { // If data validation fails (rating is not 1-5 or note is longer than 255)
            return ResponseEntity.badRequest().body("Invalid input: " + result.getFieldErrors().get(0).getDefaultMessage());
        }

        try {
            userGameService.updateUserGame(request,id);
            return ResponseEntity.ok("Game updated!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Takes the id from the URL that corresponds to an entry in the user's game list and removes it if it exists
     *
     * @param gameId the id of the entry to be removed from the user's game list
     * @return ok status if game was successfully removed, or a not found response when game id isn't in user's game list
     */
    @DeleteMapping("/{gameId}")
    public ResponseEntity<String> deleteUserGame(@PathVariable Long gameId) {
        try {
            userGameService.deleteUserGame(gameId);
            return ResponseEntity.ok("Game removed from your list.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

