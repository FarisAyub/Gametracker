package com.faris.gametracker.repository;

import com.faris.gametracker.model.UserAccount;
import com.faris.gametracker.model.UserGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGameRepository extends JpaRepository<UserGame, Long> {
    // Spring breaks down method and converts it to check if a record exists with the game id passed in, by gameId lets it know that we're checking for the id
    // Could overwrite with @Query if spring didn't know how to interpret it, but it's unnecessary in this case.
    boolean existsByGameIdAndUser(Long gameId, UserAccount user);

    List<UserGame> findByUser(UserAccount user);

    // Count of games user has in their list
    long countByUser(UserAccount user);

    @Query("SELECT AVG(ug.rating) FROM UserGame ug WHERE ug.user = :user")
    Double findAverageRatingByUser(@Param("user") UserAccount user);
}
