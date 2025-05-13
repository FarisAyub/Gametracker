package com.faris.gametracker.controller;

import org.springframework.ui.Model;
import com.faris.gametracker.dto.RegisterRequest;
import com.faris.gametracker.model.UserAccount;
import com.faris.gametracker.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegisterController {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterController(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") RegisterRequest dto, Model model) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            model.addAttribute("error", "Passwords do not match.");
            return "register";
        }

        if (userAccountRepository.findByUsername(dto.getUsername()).isPresent()) {
            model.addAttribute("error", "Username already exists.");
            return "register";
        }

        UserAccount user = new UserAccount();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());

        userAccountRepository.save(user);

        return "redirect:/login?registered";
    }
}
