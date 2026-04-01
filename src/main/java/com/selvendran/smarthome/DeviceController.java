package com.selvendran.smarthome;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/devices")
@CrossOrigin
public class DeviceController {

    private final DeviceRepository deviceRepo;
    private final RoomRepository roomRepo;

    public DeviceController(DeviceRepository deviceRepo,
                            RoomRepository roomRepo) {
        this.deviceRepo = deviceRepo;
        this.roomRepo = roomRepo;
    }


    @GetMapping("/status/{deviceId}")
public boolean getDeviceStatus(@PathVariable Long deviceId) {

    Device device = deviceRepo.findById(deviceId).orElse(null);

    if (device == null) {
        return false;
    }

    return device.isStatus();
}

    // TOGGLE METHOD
    @PutMapping("/toggle/{id}")
    public Device toggleDevice(@PathVariable Long id) {

        Device device = deviceRepo.findById(id).orElseThrow();

        device.setStatus(!device.isStatus());
        deviceRepo.save(device);

        sendCommandToESP(device.getId(), device.isStatus());

        return device;
    }

    // 🔥 THIS METHOD MUST BE HERE
    private void sendCommandToESP(Long deviceId, boolean status) {

        String espIp = "localhost"; // change this

        String url = "http://" + espIp +
                     "/control?device=" + deviceId +
                     "&state=" + (status ? "ON" : "OFF");

        try {
            new java.net.URL(url).openStream().close();
            System.out.println("Command sent to ESP: " + url);
        } catch (Exception e) {
            System.out.println("ESP not reachable");
        }
    }

}