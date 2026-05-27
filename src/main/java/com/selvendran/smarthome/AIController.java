package com.selvendran.smarthome;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Smart Home AI assistant — plain-text answers via local Ollama.
 */
@RestController
@CrossOrigin
@RequestMapping("/ai")
public class AIController {

    private final OllamaService ollamaService;

    public AIController(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    /**
     * POST /ai/ask — preferred body {@code {"model":"phi3","question":"hello"}}.
     * Also accepts legacy {@code prompt} without {@code model} (uses default from configuration).
     * Response: {@code text/plain} assistant text only.
     */
    @PostMapping(value = "/ask", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> ask(@RequestBody Map<String, String> body) {
        String question = body.get("question");
        if (question == null || question.isBlank()) {
            question = body.get("prompt");
        }
        if (question == null) {
            question = "";
        }
        String model = body.get("model");
        String answer = ollamaService.generate(question, model);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(answer);
    }
}
