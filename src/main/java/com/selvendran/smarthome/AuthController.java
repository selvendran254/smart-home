package com.selvendran.smarthome;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AuthController {

    private final UserRepository userRepository;
    private final DoorNotifyService doorNotifyService;

    public AuthController(UserRepository userRepository, DoorNotifyService doorNotifyService) {
        this.userRepository = userRepository;
        this.doorNotifyService = doorNotifyService;
    }

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

        if (user.getUsername() == null || user.getUsername().isBlank()) {
            doorNotifyService.notifyLoginFailed("(none)", "invalid_request");
            return "Enter username";
        }

        String attemptUser = user.getUsername().strip();
        User existing = userRepository.findByUsername(attemptUser);

        if (existing == null) {
            doorNotifyService.notifyLoginFailed(attemptUser, "user_not_found");
            return "User not found";
        }

        if (!existing.getPassword().equals(user.getPassword())) {
            doorNotifyService.notifyLoginFailed(attemptUser, "wrong_password");
            return "Wrong password";
        }

        session.setAttribute("user", existing.getUsername());
        doorNotifyService.notifyLoginSuccess(existing.getUsername());
        return "Login success";
    }

    @GetMapping("/check-session")
    public String checkSession(HttpSession session) {
        return session.getAttribute("user") == null ? "NO" : "YES";
    }

    /** Logged-in server account (separate from local “profile” in the UI). */
    @GetMapping("/auth/me")
    public ResponseEntity<Map<String, Object>> authMe(HttpSession session) {
        Object u = session.getAttribute("user");
        if (u == null) {
            return ResponseEntity.status(401).body(Map.of("loggedIn", false));
        }
        return ResponseEntity.ok(Map.of("loggedIn", true, "username", u.toString()));
    }

    @GetMapping("/logout")
    public void logout(HttpSession session) {
        session.invalidate();
    }
}
