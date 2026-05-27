package com.selvendran.smarthome;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin
public class PlantController {

    private final OllamaService ollamaService;

    public PlantController(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    @PostMapping("/plant/analyze")
    public String analyzePlantImage(@RequestParam("image") MultipartFile file) {
        if (file.isEmpty()) {
            return "Error: Empty file.";
        }
        try {
            return ollamaService.analyzePlantWithImage(file.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return "Error analyzing image: " + e.getMessage();
        }
    }
}
