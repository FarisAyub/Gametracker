package com.faris.gametracker.controller;

import com.faris.gametracker.model.UserAccount;
import com.faris.gametracker.model.UserGame;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import com.faris.gametracker.repository.UserAccountRepository;
import com.faris.gametracker.repository.UserGameRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserGameRepository userGameRepository;
    private final UserAccountRepository userAccountRepository;

    public ProfileController(UserGameRepository userGameRepository, UserAccountRepository userAccountRepository) {
        this.userGameRepository = userGameRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        // Get logged-in user
        UserAccount user = userAccountRepository.findByUsername(userDetails.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));

        long completed = userGameRepository.countByUser(user);
        double avgRating = userGameRepository.findAverageRatingByUser(user);
        String fullName = user.getFirstName() + " " + user.getLastName();

        model.addAttribute("username", user.getUsername());
        model.addAttribute("fullName", fullName);
        model.addAttribute("email", user.getEmail());
        model.addAttribute("completed", completed); // Games count
        model.addAttribute("avgRating", avgRating); // Avg rating
        return "profile";
    }
}
