package com.faris.gametracker.repository;

import com.faris.gametracker.model.UserGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGameRepository extends JpaRepository<UserGame, Long> {
    // Spring breaks down method and converts it to check if a record exists with the game id passed in, by gameId lets it know that we're checking for the id
    // Could overwrite with @Query if spring didn't know how to interpret it, but it's unnecessary in this case.
    boolean existsByGameId(Long gameId);
}
