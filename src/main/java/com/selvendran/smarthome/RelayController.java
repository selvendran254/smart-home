package com.selvendran.smarthome;

import org.springframework.web.bind.annotation.*;
import java.net.HttpURLConnection;
import java.net.URL;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/relay")
public class RelayController {

    // 🔴 CHANGE THIS to your ESP32 IP
    private final String espIp = "http://10.170.88.228";

    @GetMapping("/{num}/{state}")
    public String controlRelay(@PathVariable int num,
                               @PathVariable String state) {

        try {
            // Build ESP URL
            String espUrl = espIp + "/relay?num=" + num + "&state=" + state;

            System.out.println("Calling ESP: " + espUrl);

            URL url = new URL(espUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            System.out.println("ESP Response Code: " + responseCode);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }

        return "OK";
    }
}