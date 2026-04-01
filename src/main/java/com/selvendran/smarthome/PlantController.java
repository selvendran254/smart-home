package com.selvendran.smarthome;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;

@RestController
@CrossOrigin
public class PlantController {

    @PostMapping("/plant/analyze")
    public String analyzePlantImage(@RequestParam("image") MultipartFile file) {
        if (file.isEmpty()) {
            return "Error: Empty file.";
        }

        try {
            // Save the uploaded file to a temporary location
            File tempFile = File.createTempFile("plant_temp_", "_" + file.getOriginalFilename());
            file.transferTo(tempFile);

            // Execute the python script with the absolute path of the image
            ProcessBuilder pb = new ProcessBuilder("python", "plant_vision.py", tempFile.getAbsolutePath());
            pb.environment().put("PYTHONUTF8", "1");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
            
            // Delete temp file after analysis
            tempFile.delete();

            String responseText = output.toString().trim();
            if (responseText.isEmpty()) {
                return "No AI response received.";
            }

            return responseText;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error analyzing image: " + e.getMessage();
        }
    }
}
