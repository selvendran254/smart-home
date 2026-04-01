package com.selvendran.smarthome;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/sensor")
@CrossOrigin
public class SensorController {

    private final SensorDataRepository repository;

    public SensorController(SensorDataRepository repository) {
        this.repository = repository;
    }

    /* =====================================
       ESP32 SENDS SENSOR DATA HERE
       POST http://SERVER_IP:8080/sensor
    ====================================== */
    @PostMapping
    public SensorData saveData(@RequestBody SensorData data) {

        return repository.save(data);
    }


    /* =====================================
       GET LATEST SENSOR DATA
       GET http://SERVER_IP:8080/sensor/latest
    ====================================== */
    @GetMapping("/latest")
public ResponseEntity<?> getLatest() {

    List<SensorData> list = repository.findAll();

    if (list.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    return ResponseEntity.ok(list.get(list.size() - 1));
}


    /* =====================================
       GET FULL SENSOR HISTORY
       GET http://SERVER_IP:8080/sensor/all
    ====================================== */
    @GetMapping("/all")
    public List<SensorData> getAll() {

        return repository.findAll();
    }
}