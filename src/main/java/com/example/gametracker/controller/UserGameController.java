package com.example.gametracker.controller;

import com.example.gametracker.dto.UserGameRequest;
import com.example.gametracker.dto.UserGameResponse;
import com.example.gametracker.model.Game;
import com.example.gametracker.model.UserGame;
import com.example.gametracker.repository.GameRepository;
import com.example.gametracker.repository.UserGameRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user-games")
public class UserGameController {

    private final UserGameRepository userGameRepository;
    private final GameRepository gameRepository;

    @Autowired
    public UserGameController(UserGameRepository userGameRepository, GameRepository gameRepository) {
        this.userGameRepository = userGameRepository;
        this.gameRepository = gameRepository;
    }

    /**
     * Lists all games in user list, filtered and sorted down by optional parameters.
     * Uses a DTO to filter the information being passed, also allows details from the Game model to be added
     *
     * @param searchQuery    Optional parameter that can be passed in by searching on the website, text is filtered by this
     * @param sortBy         Optional parameter that accepts values "rating" "releaseDate" and "title" and sorts alphabetically based on the parameter
     * @param filterByRating Optional parameter that takes in an integer from -1 to 5 to be used to filter games that have this rating
     * @param page           Optional parameter that takes in the current page on user-games. Defaults to 0 for when a page hasn't been passed in
     * @param size           Optional parameter that takes a size for pages. Defaults to 9 user games per page unless modified
     * @param model          Used to return information back to the thymeleaf html page
     * @return the model and list of games and other variables back to the user-games page
     */
    @GetMapping
    public String getAllUserGames(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) Integer filterByRating,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "9") Integer size,
            Model model) {

        List<UserGame> allUserGames = userGameRepository.findAll();
        List<UserGameResponse> dtoUserGames = new ArrayList<>(); // Array using dto

        allUserGames = filterBySearch(allUserGames, searchQuery); // Filters game with search query if it exists
        allUserGames = filterByRating(allUserGames, filterByRating); // Filter list by rating if it exists
        allUserGames = sortUserGames(allUserGames, sortBy); // Sort list of games by sort string if it exists

        // Filter the list into pages by setting a start and end index for the current page
        int start = page * size; // Get the current index for the new page
        int end = Math.min(start + size, allUserGames.size()); // Get the last game for the current page, checking the size to return a smaller list if not enough left

        // Create a new list that's a sublist of games showing the amount of games per page requested
        List<UserGame> pagedGames = allUserGames.subList(start, end);

        // Booleans to be used by the page buttons, if there's no more games to left/right, disable button for moving page
        boolean hasNext = end < allUserGames.size();
        boolean hasPrevious = page > 0;

        // For each object in our users games list, create DTO object from both the game and user game variables
        for (UserGame userGame : pagedGames) {
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
            dtoUserGames.add(userGameResponse); // Add new DTO object to our new list
        }
        // Returns everything back to the template in thymeleaf
        model.addAttribute("userGames", dtoUserGames);
        model.addAttribute("searchQuery", searchQuery);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("filterByRating", filterByRating);
        model.addAttribute("currentPage", page);
        model.addAttribute("hasNext", hasNext);
        model.addAttribute("hasPrevious", hasPrevious);
        return "user-games";
    }


    /**
     * Adds a game to the users games list, setting the rating and note provided
     *
     * @param request takes the post sent in as json to parse
     * @param result  gets populated if data validation for request fails
     * @return message stating game was added to list, or error if it fails
     */
    @PostMapping
    @ResponseBody
    public ResponseEntity<String> addUserGame(@RequestBody @Valid UserGameRequest request, BindingResult result) {
        if (result.hasErrors()) { // If data validation fails (rating is not 1-5 or note is longer than 255)
            String errorMessage = result.getFieldErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body("Validation error: " + errorMessage);
        }

        Optional<Game> gameOpt = gameRepository.findById(request.getGameId()); // Optional as the gameId passed in may not exist

        if (gameOpt.isEmpty()) { // If no game exists with the id passed
            return ResponseEntity.badRequest().body("Game not found.");
        }

        if (userGameRepository.existsByGameId(request.getGameId())) { // Check if the game is already in the users list, if so don't add
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Game is already in your list.");
        }

        UserGame userGame = new UserGame(); // Create a new user game and assign the values to it
        userGame.setGame(gameOpt.get()); // Set the game
        userGame.setRating(request.getRating()); // Set the rating
        userGame.setNote(request.getNote()); // Set the note
        userGameRepository.save(userGame); // Add to the database

        return ResponseEntity.ok("Game added to your list!"); // Return message
    }

    /**
     * Takes in an id from the URL and updates the User Game entry with the id passed. Uses the JSON data passed in
     * to overwrite the old values for the corresponding id
     *
     * @param id      The id from the url corresponding to the entry that needs to be updated
     * @param request The JSON body containing the updated rating and note
     * @param result  A binding result that will be populated if the request passed in doesn't meet the data validation rules (rating 1-5 and note max 255 chars)
     * @return returns ok status if successfully updated
     */
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<String> updateUserGame(@PathVariable Long id, @RequestBody @Valid UserGameRequest request, BindingResult result) {
        if (result.hasErrors()) { // If data validation fails (rating is not 1-5 or note is longer than 255)
            return ResponseEntity.badRequest().body("Invalid input: " + result.getFieldErrors().get(0).getDefaultMessage());
        }

        Optional<UserGame> userGameOpt = userGameRepository.findById(id);
        if (userGameOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserGame userGame = userGameOpt.get();
        userGame.setRating(request.getRating());
        userGame.setNote(request.getNote());
        userGameRepository.save(userGame);

        return ResponseEntity.ok("Game updated.");
    }

    /**
     * Takes the id from the URL that corresponds to an entry in the user's game list and removes it if it exists
     *
     * @param gameId the id of the entry to be removed from the user's game list
     * @return ok status if game was successfully removed, or a not found response when game id isn't in user's game list
     */
    @DeleteMapping("/{gameId}")
    public ResponseEntity<String> deleteUserGame(@PathVariable Long gameId) {
        if (!userGameRepository.existsById(gameId)) {
            return ResponseEntity.notFound().build(); // If user game not found return error
        }
        // Delete the game
        userGameRepository.deleteById(gameId);
        // Return successful message
        return ResponseEntity.ok("Game removed from your list.");
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
        if (rating == null || rating == -1)
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

