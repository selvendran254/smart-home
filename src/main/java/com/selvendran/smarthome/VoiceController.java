package com.selvendran.smarthome;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@RestController
@CrossOrigin
public class VoiceController {

    private final String espIp = "http://10.227.61.228"; // change if needed

// API key moved to gemini_assistant.py

    @PostMapping("/voice-command")
    public String handleVoice(@RequestBody Map<String, String> body) {
        String command = body.get("command");
        if (command == null)
            return "No command received";

        String lowerCommand = command.toLowerCase();

        // 🔌 SMART HOME CONTROL
        if ((lowerCommand.contains("light") || lowerCommand.contains("fan")) &&
                (lowerCommand.contains("on") || lowerCommand.contains("off"))) {

            int number = extractNumber(lowerCommand);
            if (number == 0) {
                return "Please specify a device number (1-6).";
            }

            String device = lowerCommand.contains("light") ? "light" : "fan";
            String state = lowerCommand.contains("on") ? "on" : "off";

            String url = espIp + "/" + device + "/" + number + "/" + state;

            try {
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getForObject(url, String.class);
                return "Turned " + state + " " + device + " " + number;
            } catch (Exception e) {
                return "Device not reachable";
            }
        }

        // 🤖 AI RESPONSE
        return getAIResponse(command);
    }

    private String getAIResponse(String prompt) {
        try {
            // Call the python script we created
            ProcessBuilder pb = new ProcessBuilder("python", "gemini_assistant.py", prompt);
            pb.environment().put("PYTHONUTF8", "1");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();

            String responseText = output.toString().trim();
            if (responseText.isEmpty()) {
                return "No AI response received.";
            }
            return responseText;

        } catch (Exception e) {
            return "Error calling Python script: " + e.getMessage();
        }
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