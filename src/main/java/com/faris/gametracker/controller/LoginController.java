package com.faris.gametracker.controller;

import com.faris.gametracker.model.UserAccount;
import com.faris.gametracker.repository.UserAccountRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

}
