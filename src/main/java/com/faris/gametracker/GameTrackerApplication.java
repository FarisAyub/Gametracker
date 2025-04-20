package com.faris.gametracker;

import com.faris.gametracker.service.GameApiService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class GameTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameTrackerApplication.class, args);
    }

    // Runs at startup and fetches games from the RAWG games API
    @Bean
    @Profile("!test") // Don't populate database for integration tests
    CommandLineRunner runOnStartup(GameApiService gameApiService) {
        return args -> {
            if (gameApiService.isDatabaseEmpty()) {
                gameApiService.fetchGamesApi();
            }
        };
    }

}
