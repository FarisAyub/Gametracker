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

    private final RestTemplate restTemplate = new RestTemplate();
    private final GameRepository gameRepository;

    public GameApiService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public void fetchGamesApi() {
        // Get 5 pages each showing 20 results from the database
        // Can increase to expand amount of games in the database
        int totalPages = 5;
        int pageSize = 20;

        // Loop through each page adding all games to database
        for (int page = 1; page <= totalPages; page++) {
            // Create a string using the api key + the url we want to search
            String API_KEY = "b2344a674a6546d5aee55ee030e08609";
            String API_URL = "https://api.rawg.io/api/games";
            String url = API_URL + "?key=" + API_KEY + "&page=" + page + "&page_size=" + pageSize;

            // Get the data from the url as JSON
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode results = response.getBody().get("results");

            // Loop through each game in the JSON results for the current page, adding it to database
            for (JsonNode gameJson : results) {
                String title = gameJson.get("name").asText(); // Title of the game
                String releaseDateStr = gameJson.get("released").asText(""); // Release date as a string
                String cover = gameJson.get("background_image").asText(null); // Url of the cover image

                // We can't get developer without an advanced search since it's not included in basic api response
                String slug = gameJson.get("slug").asText(); // Use slug which contains name of the current game to search for that game specifically
                String detailUrl = API_URL + "/" + slug + "?key=" + API_KEY; // Make another api call using the specific game, to get more detailed information

                // Get the data as JSON
                ResponseEntity<JsonNode> detailResponse = restTemplate.getForEntity(detailUrl, JsonNode.class);
                JsonNode detailedGame = detailResponse.getBody();

                String developer = getDeveloper(detailedGame); // Developer of the game
                String publisher = getPublisher(detailedGame); // Publisher of the game

                LocalDate releaseDate;

                try {
                    releaseDate = LocalDate.parse(releaseDateStr); // Convert the release date from string to a LocalDate object
                } catch (DateTimeParseException e) {
                    continue; // Skip if date is invalid
                }

                // Create a new game with all the details from the api
                Game game = new Game(cover, title, releaseDate, developer, publisher);

                // Add the game to our database
                if (gameRepository.findByTitleIgnoreCase(title).isEmpty()) {
                    gameRepository.save(game);
                }
            }
        }
    }

    /**
     * Takes JSON from an advanced PI search, which contains details about the one specific game, we return the first listed developer of the game
     *
     * @param detailedGame JSON containing all API details about a specific game
     * @return either the first listed developer of the game, or "Unknown" if none exist
     */
    // Have to run an advanced api search to get developer
    private String getDeveloper(JsonNode detailedGame) {
        JsonNode developers = detailedGame.get("developers");
        if (developers != null && developers.isArray() && !developers.isEmpty()) {
            return developers.get(0).get("name").asText("Unknown");
        }
        return "Unknown";
    }

    /**
     * Takes JSON from an advanced API search, which contains details about one specific game, we then return the first
     * listed publisher of that game, or we return "Unknown" if there are none listed
     *
     * @param detailedGame JSON containing advanced details about a specific game
     * @return either the first listed publisher of the game, or "Unknown"
     */
    // Have to run an advanced api search to get publisher
    private String getPublisher(JsonNode detailedGame) {
        JsonNode publishers = detailedGame.get("publishers");
        if (publishers != null && publishers.isArray() && !publishers.isEmpty()) {
            return publishers.get(0).get("name").asText("Unknown");
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
