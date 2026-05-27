package com.selvendran.smarthome;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@CrossOrigin
public class VoiceController {

    private static final Logger log = LoggerFactory.getLogger(VoiceController.class);

    private final String espIp = "http://10.227.61.228"; // change if needed

    private final OllamaService ollamaService;

    public VoiceController(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    @PostConstruct
    void logVoiceRoutes() {
        log.info("Voice/Ollama: GET /api/voice/ai-info and GET /voice/ai-info (restart app if 404 in browser)");
    }

    /**
     * Open in browser after restart: JSON with provider=ollama. 404 = old JAR or app not restarted after pull/build.
     */
    @GetMapping({ "/api/voice/ai-info", "/voice/ai-info" })
    public Map<String, String> voiceAiInfo() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("provider", "ollama");
        m.put("model", ollamaService.getModel());
        m.put("chatModels", String.join(",", OllamaService.CHAT_MODELS));
        m.put("visionModel", ollamaService.getVisionModel());
        m.put("generateUrl", ollamaService.getGenerateUrl());
        return m;
    }

    @PostMapping("/voice-command")
    public ResponseEntity<String> handleVoice(@RequestBody Map<String, String> body) {
        String command = body.get("command");
        if (command == null) {
            return voiceOllama(ResponseEntity.ok(), "No command received");
        }
        String modelOverride = body.get("model");

        String lowerCommand = command.toLowerCase();

        // 🔌 SMART HOME CONTROL
        if ((lowerCommand.contains("light") || lowerCommand.contains("fan")) &&
                (lowerCommand.contains("on") || lowerCommand.contains("off"))) {

            int number = extractNumber(lowerCommand);
            if (number == 0) {
                return voiceOllama(ResponseEntity.ok(), "Please specify a device number (1-6).");
            }

            String device = lowerCommand.contains("light") ? "light" : "fan";
            String state = lowerCommand.contains("on") ? "on" : "off";

            String url = espIp + "/" + device + "/" + number + "/" + state;

            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getForObject(url, String.class);
                return voiceOllama(ResponseEntity.ok(), "Turned " + state + " " + device + " " + number);
            } catch (Exception e) {
                return voiceOllama(ResponseEntity.ok(), "Device not reachable");
            }
        }

        // 🤖 Local Ollama (optional JSON "model": "phi3" | "llama3")
        return voiceOllama(ResponseEntity.ok(), getAIResponse(command, modelOverride));
    }

    private static ResponseEntity<String> voiceOllama(ResponseEntity.BodyBuilder builder, String body) {
        return builder.header("X-Voice-AI-Provider", "ollama").body(body);
    }

    private String getAIResponse(String prompt, String modelOverride) {
        return ollamaService.generate(prompt, modelOverride);
    }

    private int extractNumber(String command) {
        for (int i = 1; i <= 6; i++) {
            if (command.contains(String.valueOf(i))) {
                return i;
            }
        }
        return 0;
    }
}