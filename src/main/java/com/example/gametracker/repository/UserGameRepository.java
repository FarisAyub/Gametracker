package com.example.gametracker.repository;

import com.example.gametracker.model.UserGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGameRepository extends JpaRepository<UserGame, Long> {
    // Spring breaks down method and converts it to check if a record exists with the game id passed in, by gameid lets it know that we're checking for the id
    // Could overwrite with @Query, but it's unnecessary.
    boolean existsByGameId(Long gameId);
}
