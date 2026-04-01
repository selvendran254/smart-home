package com.selvendran.smarthome;

import org.springframework.web.bind.annotation.*;
import java.io.*;

@RestController
public class DoorController {

  @GetMapping("/door/unlock")
public String unlockDoor() {

    try {

        ProcessBuilder pb = new ProcessBuilder(
                "cmd", "/k",
                "C:\\Users\\Selvendran\\AppData\\Local\\Programs\\Python\\Python313\\python.exe",
                "C:\\Users\\Selvendran\\OneDrive\\Desktop\\FaceProject\\unlock.py"
        );

        pb.start();

        return "Face Recognition Started";

    } catch (Exception e) {
        e.printStackTrace();
        return "Error starting face recognition";
    }
}

  @PostMapping("/door/unlock-pin")
  public String unlockDoorWithPin() {
      // In a real scenario, this might trigger an ESP32 or a relay switch.
      // For now, we simulate a successful unlock.
      System.out.println("Door Unlocked securely via Profile PIN");
      return "Success";
  }
}