package com.faris.gametracker.service;

import com.faris.gametracker.model.Game;
import com.faris.gametracker.repository.GameRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GameApiServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameApiService gameApiService;

    @Test
    public void parseGameFromJson_ShouldReturnGame() {
        // Mock JSON response from API
        ObjectNode gameJson = mapper.createObjectNode();
        gameJson.put("name", "Witcher 3");
        gameJson.put("released", "2025-01-01");
        gameJson.put("background_image", "url");

        // Mock JSON advanced details for developer and publisher
        ObjectNode dev = mapper.createObjectNode();
        dev.put("name", "CD Projekt Red");

        ObjectNode pub = mapper.createObjectNode();
        pub.put("name", "CDPR");

        ArrayNode devs = mapper.createArrayNode().add(dev);
        ArrayNode pubs = mapper.createArrayNode().add(pub);

        ObjectNode detailedGame = mapper.createObjectNode();
        detailedGame.set("developers", devs);
        detailedGame.set("publishers", pubs);

        // Create game object using mock JSON's
        Game result = gameApiService.parseGameFromJson(gameJson, detailedGame);

        assertNotNull(result); // Game was returned
        assertEquals("Witcher 3", result.getTitle()); // Title is correct
        assertEquals(LocalDate.of(2025, 1, 1), result.getReleaseDate()); // Date is correct
        assertEquals("CD Projekt Red", result.getDeveloper()); // Developer is correct
        assertEquals("CDPR", result.getPublisher()); // Publisher is correct
    }

    @Test
    public void isDatabaseEmpty_ShouldReturnTrue() {
        when(gameRepository.count()).thenReturn(0L); // Simulate empty database
        assertTrue(gameApiService.isDatabaseEmpty()); // Check that the method returns true for empty database
    }

    @Test
    public void isDatabaseEmpty_ShouldReturnFalse() {
        when(gameRepository.count()).thenReturn(10L); // Simulate 10 games in database
        assertFalse(gameApiService.isDatabaseEmpty()); // Check the method returns false for empty database
    }

}
