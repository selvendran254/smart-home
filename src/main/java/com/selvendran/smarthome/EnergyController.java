package com.selvendran.smarthome;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/energy")
@CrossOrigin
public class EnergyController {

    @GetMapping
    public List<Map<String, Object>> getEnergyData() {
        return List.of(
            Map.of("date", "Mon", "units", 12.5),
            Map.of("date", "Tue", "units", 15.0),
            Map.of("date", "Wed", "units", 10.2),
            Map.of("date", "Thu", "units", 18.5),
            Map.of("date", "Fri", "units", 14.1),
            Map.of("date", "Sat", "units", 20.0),
            Map.of("date", "Sun", "units", 22.5)
        );
    }
}
