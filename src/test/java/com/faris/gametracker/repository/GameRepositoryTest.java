package com.faris.gametracker.repository;

import com.faris.gametracker.model.Game;
import com.faris.gametracker.service.GameApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class GameRepositoryTest {

    @Autowired
    private GameRepository gameRepository;

    @MockBean
    private GameApiService gameApiService;

    private Game game;

    @BeforeEach
    public void setup() {
        game = new Game("url", "The Witcher 3", LocalDate.of(2001, Month.MAY, 5), "CD Projekt Red", "CDPR");
        gameRepository.save(game); // Add our test game to database
    }

    @Test
    public void findByTitleIgnoreCase_Capitalised_ShouldReturnGame() {
        Optional<Game> foundGame = gameRepository.findByTitleIgnoreCase("THE WITCHER 3");

        assertThat(foundGame.isPresent()).isTrue(); // Game should be found (optional not empty)
        assertEquals(foundGame.get(), game); // Make sure it is the correct game
    }

    @Test
    public void findByTitleIgnoreCase_LowerCase_ShouldReturnGame() {
        Optional<Game> gameOptional = gameRepository.findByTitleIgnoreCase("the witcher 3");

        assertThat(gameOptional.isPresent()).isTrue(); // Game should be found (optional not empty)
        assertEquals(gameOptional.get(), game); // Make sure it is the correct game

    }

    @Test
    public void findByTitleIgnoreCase_GameDoesntExist_ShouldReturnEmptyOptional() {
        Optional<Game> gameOptional = gameRepository.findByTitleIgnoreCase("elden ring"); // Doesn't exist

        assertThat(gameOptional.isPresent()).isFalse(); // Optional should be empty
    }

}
