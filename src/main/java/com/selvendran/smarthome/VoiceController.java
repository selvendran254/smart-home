package com.selvendran.smarthome;

import org.springframework.web.client.RestTemplate;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
public class VoiceController {

    @PostMapping("/voice-command")
    public String handleVoice(@RequestBody Map<String, String> body) {

        String command = body.get("command").toLowerCase();

        int number = extractNumber(command);

        if (number == 0) {
            return "Invalid device number";
        }

        // LIGHT CONTROL
        if (command.contains("light")) {

            if (command.contains("on")) {
                callESP("light", number, "on");
                return "Light " + number + " ON";
            }

            if (command.contains("off")) {
                callESP("light", number, "off");
                return "Light " + number + " OFF";
            }
        }

        // FAN CONTROL
        if (command.contains("fan")) {

            if (command.contains("on")) {
                callESP("fan", number, "on");
                return "Fan " + number + " ON";
            }

            if (command.contains("off")) {
                callESP("fan", number, "off");
                return "Fan " + number + " OFF";
            }
        }

        return "Unknown Command";
    }

    private int extractNumber(String command) {
        for (int i = 1; i <= 6; i++) {
            if (command.contains(String.valueOf(i))) {
                return i;
            }
        }
        return 0;
    }

    private void callESP(String device, int number, String state) {
        String espIp = "http://10.170.88.228";  // CHANGE THIS to your ESP32 IP

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getForObject(
            espIp + "/" + device + "/" + number + "/" + state,
            String.class
        );
    }
}