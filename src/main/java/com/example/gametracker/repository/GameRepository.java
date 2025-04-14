package com.example.gametracker.repository;

import com.example.gametracker.model.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByTitleIgnoreCase(String title);
    Page<Game> findAll(Pageable pageable);
}
