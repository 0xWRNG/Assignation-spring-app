package com.example.auth_spring.controller;

import com.example.auth_spring.model.User;
import com.example.auth_spring.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/register")
    public String showRegisterForm(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            return "redirect:/login";
        }
        return "register";
    }


    @PostMapping("/register")
    public String registerUser(User user, Model model) {
        logger.info("Displaying register form");
        try {
            userService.register(user);
            logger.info("Successfully registered: {}", user.getLogin());
            return "redirect:/login";

        } catch (Exception e) {
            logger.error("Registration failed for: {}", user.getLogin(), e);
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                                    @RequestParam(value = "logout", required = false) String logout,
                                    @AuthenticationPrincipal UserDetails userDetails, Model model) {
        logger.info("Displaying login form");
        if (userDetails != null) {
            logger.info("User already logged in, redirecting to profile.");
            return "redirect:/profile";
        }
        if (error != null) {
            model.addAttribute("error", "login or password is incorrect");
        }
        if (logout != null) {
            model.addAttribute("logout", "You have been logged out");
        }
        return "login";
    }

}
