package com.example.auth_spring.controller;
import com.example.auth_spring.model.enums.Role;
import com.example.auth_spring.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.example.auth_spring.service.UserService;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserService userService;

    @GetMapping("")
    public String showAdminPanel(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("users", userService.getAllUsers());
        int current_id = userService.getUserByLogin(userDetails.getUsername()).getId();
        model.addAttribute("current_id", current_id);
        return "admin_panel";
    }
    @PostMapping("upd-role/{id}/role")
    public ResponseEntity<String> updateRole(@PathVariable int id, @RequestParam String role) {
        try {
            User user = userService.getUserById(id);
            user.setRole(role);
            userService.saveUser(user);
            return ResponseEntity.ok("Role updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update role: " + e.getMessage());
        }
    }
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable int id) {
        try {
            userService.deleteUser(id);
            return "redirect:/admin";
        } catch (Exception e) {
            return "redirect:/admin?error=" + e.getMessage();
        }
    }
}
