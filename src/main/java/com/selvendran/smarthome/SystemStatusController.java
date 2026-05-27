package com.selvendran.smarthome;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class SystemStatusController {

    private final OllamaService ollamaService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String espBaseUrl;

    public SystemStatusController(
            OllamaService ollamaService,
            @Value("${smarthome.esp.base-url:http://localhost}") String espBaseUrl) {
        this.ollamaService = ollamaService;
        this.espBaseUrl = espBaseUrl.replaceAll("/$", "");
    }

    @GetMapping("/api/system/status")
    public Map<String, Object> status() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ollama", checkOllama());
        out.put("esp", checkEsp());
        return out;
    }

    private Map<String, Object> checkOllama() {
        Map<String, Object> m = new LinkedHashMap<>();
        try {
            restTemplate.getForObject(ollamaService.getBaseUrl() + "/api/tags", Map.class);
            m.put("online", true);
            m.put("label", ollamaService.getModel());
        } catch (Exception e) {
            m.put("online", false);
            m.put("label", "Offline");
        }
        return m;
    }

    private Map<String, Object> checkEsp() {
        Map<String, Object> m = new LinkedHashMap<>();
        try {
            restTemplate.getForObject(espBaseUrl + "/relay?num=1&state=0", String.class);
            m.put("online", true);
            m.put("label", "Connected");
        } catch (Exception e) {
            m.put("online", false);
            m.put("label", "Unreachable");
        }
        return m;
    }
}
