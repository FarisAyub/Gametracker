package com.faris.gametracker.service;

import com.faris.gametracker.dto.PageResponse;
import com.faris.gametracker.model.Game;
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
    private final PaginationService paginationService;
    private final FilterService filterService;

    public GameService(GameRepository gameRepository, UserGameRepository userGameRepository, PaginationService paginationService, FilterService filterService) {
        this.gameRepository = gameRepository;
        this.userGameRepository = userGameRepository;
        this.paginationService = paginationService;
        this.filterService = filterService;
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

    /**
     * Takes in Filters, page and size of page and returns a single page of games after filtering
     *
     * @param filterSearch String containing searched value
     * @param filterSort Sorting option for ordering
     * @param filterList Filtering option to hide/show games in list
     * @param page Current page number, to paginate list
     * @param size Size of each page, to paginate list
     * @return PageResponse which contains List of games, and whether there's a next/previous page
     */
    public PageResponse<Game> getGames(String filterSearch, String filterSort, String filterList, Integer page, Integer size) {
        // Filter all games
        List<Game> filtered = filterService.filterGames(gameRepository.findAll(), filterSearch, filterSort, filterList, getGamesInList());

        // Paginate the filtered list
        return paginationService.paginate(filtered, page, size);
    }
}
