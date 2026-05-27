package com.selvendran.smarthome.controller;

import com.selvendran.smarthome.entity.DeviceData;
import com.selvendran.smarthome.service.DeviceDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/device")
public class DeviceDataController {

    private final DeviceDataService deviceDataService;

    @Autowired
    public DeviceDataController(DeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveDeviceData(@RequestBody DeviceData data) {
        try {
            DeviceData savedData = deviceDataService.saveDeviceData(data);
            return new ResponseEntity<>(savedData, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error saving device data: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllDeviceData() {
        try {
            List<DeviceData> allData = deviceDataService.getAllDeviceData();
            return new ResponseEntity<>(allData, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching data: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDeviceDataById(@PathVariable Long id) {
        try {
            Optional<DeviceData> data = deviceDataService.getDeviceDataById(id);
            if (data.isPresent()) {
                return new ResponseEntity<>(data.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Device data not found for id: " + id, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error fetching data: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDeviceData(@PathVariable Long id) {
        try {
            boolean isDeleted = deviceDataService.deleteDeviceData(id);
            if (isDeleted) {
                return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Device data not found for id: " + id, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting data: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
