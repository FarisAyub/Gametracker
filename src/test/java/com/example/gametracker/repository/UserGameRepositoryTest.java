package com.example.gametracker.repository;

import com.example.gametracker.model.Game;
import com.example.gametracker.model.UserGame;
import com.example.gametracker.service.GameApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserGameRepositoryTest {

    @Autowired
    private UserGameRepository userGameRepository;

    @MockBean
    private GameApiService gameApiService;

    @Test
    public void existsByGameId_Exists_ShouldReturnTrue() {
        UserGame added = userGameRepository.save(new UserGame()); // Add a game to user's list
        boolean exists = userGameRepository.existsById(added.getId()); // Check that the id of the game we added exists

        assertTrue(exists);
    }

    @Test
    public void existsByGameId_DoesNotExist_ShouldReturnFalse() {
        userGameRepository.save(new UserGame()); // Add a game to the database
        boolean exists = userGameRepository.existsById(999999L); // Game with id 999999 should not exist

        assertFalse(exists);
    }
}
