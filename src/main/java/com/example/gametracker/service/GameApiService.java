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
        // Get 5 pages on 20 results from the database, could increase but that's out of the scope of the project
        int totalPages = 5;
        int pageSize = 20;

        // Loop through each page adding them to the database
        for (int page = 1; page <= totalPages; page++) {
            // Create a string using the api key + the page we want to search
            // Replace with your actual API key
            String API_KEY = "b2344a674a6546d5aee55ee030e08609";
            String API_URL = "https://api.rawg.io/api/games";
            String url = API_URL + "?key=" + API_KEY + "&page=" + page + "&page_size=" + pageSize;

            // Get the returned results
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode results = response.getBody().get("results");

            // Loop through the current page of jsons that was returned, getting the values, creating an object
            // And then adding it to the database for each entry.
            for (JsonNode gameJson : results) {
                String title = gameJson.get("name").asText(); // Title of the game
                String releaseDateStr = gameJson.get("released").asText(""); // Release date as a string
                String cover = gameJson.get("background_image").asText(null); // Url of the cover image

                // We can't get developer without an advanced search since it's not included in basic response
                String slug = gameJson.get("slug").asText(); // Use slug which contains name of the current game
                String detailUrl = API_URL + "/" + slug + "?key=" + API_KEY; // Make another api call using the specific game

                // Returns advanced details by searching the api for the specific game we're looking at
                ResponseEntity<JsonNode> detailResponse = restTemplate.getForEntity(detailUrl, JsonNode.class);
                JsonNode detailedGame = detailResponse.getBody();

                // Use our getDeveloper/Publisher method to query the more indepth json
                String developer = getDeveloper(detailedGame);
                String publisher = getPublisher(detailedGame);

                // Create LocalDate value for our database
                LocalDate releaseDate;
                // Try to set release date to the string we received, if it's valid
                try {
                    releaseDate = LocalDate.parse(releaseDateStr);
                } catch (DateTimeParseException e) {
                    continue; // Skip if date is invalid
                }

                // Create a new game object with the details from the api
                Game game = new Game(cover, title, releaseDate, developer, publisher);

                // Add the game to our database
                if (gameRepository.findByTitleIgnoreCase(title).isEmpty()) {
                    gameRepository.save(game);
                }
            }
        }
    }

    // Have to run an advanced api search to get developer
    private String getDeveloper(JsonNode detailedGame) {
        JsonNode developers = detailedGame.get("developers");
        if (developers != null && developers.isArray() && !developers.isEmpty()) {
            return developers.get(0).get("name").asText("Unknown");
        }
        return "Unknown";
    }

    // Have to run an advanced api search to get publisher
    private String getPublisher(JsonNode detailedGame) {
        JsonNode publishers = detailedGame.get("publishers");
        if (publishers != null && publishers.isArray() && !publishers.isEmpty()) {
            return publishers.get(0).get("name").asText("Unknown");
        }
        return "Unknown";
    }


    // Check if database is empty, use it to so we don't pull from api unless we have empty database
    public boolean isDatabaseEmpty() {
        return gameRepository.count() == 0;
    }
}
