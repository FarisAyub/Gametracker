package com.faris.gametracker.service;

import com.faris.gametracker.model.Game;
import com.faris.gametracker.repository.GameRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Service
public class GameApiService {

    private final RestTemplate restTemplate;
    private final GameRepository gameRepository;

    @Value("${rawg.api.key}")
    private String apiKey;

    @Value("${rawg.api.url}")
    private String apiUrl;

    public GameApiService(GameRepository gameRepository, RestTemplate restTemplate) {
        this.gameRepository = gameRepository;
        this.restTemplate = restTemplate;
    }

    public void fetchGamesApi() {
        // Get 5 pages each of 40 results from the database (total of 200 games)
        int totalPages = 5;
        int pageSize = 40;

        // Loop through each page adding all games to database
        for (int page = 1; page <= totalPages; page++) {

            // Create the url for current page
            String url = apiUrl + "?key=" + apiKey + "&page=" + page + "&page_size=" + pageSize;

            // Get the data from the url as JSON
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode results = response.getBody().get("results");

            if (results == null || !results.isArray()) continue;

            // Loop through each game in the JSON results of the current page, adding it to the database
            for (JsonNode gameJson : results) {
                String title = gameJson.get("name").asText(); // Title of the game

                // If game is not already in database
                if (gameRepository.findByTitleIgnoreCase(title).isEmpty()) {
                    String slug = gameJson.get("slug").asText(); // Get the Slug for the current game (unique identifier)

                    // Use the Slug to get advanced details about the current game
                    String detailUrl = apiUrl + "/" + slug + "?key=" + apiKey; // Create a new URL for advanced details about each game
                    ResponseEntity<JsonNode> detailResponse = restTemplate.getForEntity(detailUrl, JsonNode.class);
                    JsonNode detailedGame = detailResponse.getBody();

                    if (detailedGame == null) continue;

                    Game game = parseGameFromJson(gameJson, detailedGame); // Pass in game and advanced game details and returns a Game object

                    // If game object was created, add to database
                    if (game != null) gameRepository.save(game);
                }

            }
        }
    }

    /**
     * Takes JSON for a game as well as a second JSON containing advanced details about a game. Using these details
     * a Game object is constructed using the details from the JSON
     *
     * @param gameJson Takes in JSON containing details about a game, which has the title, release date and cover image url
     * @param detailedGame Takes in JSON containing advanced details about a game, which has the developer and publishers
     * @return Game object with properties filled with current game's details
     */
    public Game parseGameFromJson(JsonNode gameJson, JsonNode detailedGame) {
        String title = gameJson.get("name").asText();
        String releaseDateStr = gameJson.get("released").asText("");
        String cover = gameJson.get("background_image").asText(null);
        String developer = getFirstDeveloperPublisher(detailedGame.get("developers")); // First listed developer if multiple
        String publisher = getFirstDeveloperPublisher(detailedGame.get("publishers")); // First listed publisher if multiple

        try {
            LocalDate releaseDate = LocalDate.parse(releaseDateStr); // Convert the release date from string to a LocalDate object
            return new Game(cover, title, releaseDate, developer, publisher); // Create a new game with all the details from the api
        } catch (DateTimeParseException e) {
            return null; // Skip if date is invalid
        }
    }

    /**
     * Takes JSON from an advanced API search, which contains details about one specific game, we then return the first
     * listed publisher of that game, or we return "Unknown" if there are none listed
     *
     * @param array JSON containing advanced details about a specific game
     * @return either the first listed publisher of the game, or "Unknown"
     */
    public String getFirstDeveloperPublisher(JsonNode array) {
        if (array != null && array.isArray() && !array.isEmpty()) {
            return array.get(0).get("name").asText("Unknown");
        }
        return "Unknown";
    }


    /**
     * Check if database is empty, and returns boolean of true or false depending
     *
     * @return true if the database is empty, false if it isn't
     */
    public boolean isDatabaseEmpty() {
        return gameRepository.count() == 0;
    }
}
