package com.faris.gametracker.service;

import com.faris.gametracker.model.UserGame;
import com.faris.gametracker.repository.GameRepository;
import com.faris.gametracker.repository.UserGameRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final UserGameRepository userGameRepository;

    public GameService(GameRepository gameRepository, UserGameRepository userGameRepository) {
        this.gameRepository = gameRepository;
        this.userGameRepository = userGameRepository;
    }

    /**
     * Creates a Set of Long's which are the ID's for each game in the user's game list
     *
     * @return Set of Long's representing games in user's game list
     */
    public Set<Long> getGamesInList() {
        List<UserGame> userGames = userGameRepository.findAll();
        return userGames.stream().map(userGame -> userGame.getGame().getId()).collect(Collectors.toSet());
    }
}
