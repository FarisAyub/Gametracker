package com.example.gametracker.service;

import com.example.gametracker.model.Game;
import com.example.gametracker.repository.GameRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Service
public class GameApiService {

    private final RestTemplate restTemplate;
    private final GameRepository gameRepository;

    public GameApiService(GameRepository gameRepository , RestTemplate restTemplate) {
        this.gameRepository = gameRepository;
        this.restTemplate = restTemplate;
    }

    public void fetchGamesApi() {
        // Get 5 pages each of 20 results from the database (total of 100 games)
        int totalPages = 5;
        int pageSize = 20;

        // Create a string using the api key + the url we want to search
        String API_KEY = "b2344a674a6546d5aee55ee030e08609";
        String API_URL = "https://api.rawg.io/api/games";

        // Loop through each page adding all games to database
        for (int page = 1; page <= totalPages; page++) {

            String url = API_URL + "?key=" + API_KEY + "&page=" + page + "&page_size=" + pageSize;

            // Get the data from the url as JSON
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode results = response.getBody().get("results");

            if (results == null || !results.isArray()) { // If no response from api, skip
                continue;
            }

            // Loop through each game in the JSON results for the current page, adding it to database
            for (JsonNode gameJson : results) {
                String title = gameJson.get("name").asText(); // Title of the game

                if (gameRepository.findByTitleIgnoreCase(title).isPresent()) { // If game is in list, skip
                    continue;
                }

                String slug = gameJson.get("slug").asText(); // Use slug which contains name of the current game to search for that game specifically

                // Get the data as JSON
                String detailUrl = API_URL + "/" + slug + "?key=" + API_KEY; // Make another api call using the specific game, to get more detailed information
                ResponseEntity<JsonNode> detailResponse = restTemplate.getForEntity(detailUrl, JsonNode.class);
                JsonNode detailedGame = detailResponse.getBody();

                if (detailedGame == null) { // If advanced details not found, skip game
                    continue;
                }

                Game game = parseGameFromJson(gameJson, detailedGame); // Creates game object using values from both JSON bodies

                if (game != null) { // If we have a game to add to database, then add it
                    gameRepository.save(game);
                }

            }
        }
    }

    public Game parseGameFromJson(JsonNode gameJson, JsonNode detailedGame) {
        String title = gameJson.get("name").asText();
        String releaseDateStr = gameJson.get("released").asText("");
        String cover = gameJson.get("background_image").asText(null);
        String developer = getFirstDeveloperPublisher(detailedGame.get("developers")); // First listed developer
        String publisher = getFirstDeveloperPublisher(detailedGame.get("publishers")); // First listed publisher

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
