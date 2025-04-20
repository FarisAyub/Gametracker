package com.faris.gametracker.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalControllerExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleExceptions(Exception ex, Model model) {
        model.addAttribute("errorMessage", "Oops! Something went wrong."); // Returns error message to thymeleaf on page error.html
        model.addAttribute("details", ex.getMessage()); // Sends more detailed information about the exception under the name details
        return "error"; // Redirects to the html page error.html
    }

}
