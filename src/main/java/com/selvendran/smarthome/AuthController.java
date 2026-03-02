package com.selvendran.smarthome;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@RestController
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public String register(@RequestBody User user) {

        if (userRepository.findByUsername(user.getUsername()) != null) {
            return "User already exists";
        }

        userRepository.save(user);
        return "Registered successfully";
    }

    @PostMapping("/login")
    public String login(@RequestBody User user, HttpSession session) {

        User existing = userRepository.findByUsername(user.getUsername());

        if (existing == null) return "User not found";

        if (!existing.getPassword().equals(user.getPassword()))
            return "Wrong password";

        session.setAttribute("user", existing.getUsername());
        return "Login success";
    }

    @GetMapping("/check-session")
    public String checkSession(HttpSession session) {
        return session.getAttribute("user") == null ? "NO" : "YES";
    }

    @GetMapping("/logout")
    public void logout(HttpSession session) {
        session.invalidate();
    }
}