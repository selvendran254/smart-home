package com.selvendran.smarthome;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
public class DoorController {

    private final DoorNotifyService doorNotifyService;

    public DoorController(DoorNotifyService doorNotifyService) {
        this.doorNotifyService = doorNotifyService;
    }

    /** Debug: open http://localhost:8080/door/mail-health in browser */
    @GetMapping("/door/mail-health")
    public Map<String, Object> mailHealth() {
        return doorNotifyService.mailHealth();
    }

    /** Debug: sends one test email; open http://localhost:8080/door/mail-test */
    @GetMapping("/door/mail-test")
    public String mailTest() {
        return doorNotifyService.sendTestMail();
    }

    @GetMapping("/door/unlock")
    public String unlockDoor() {
        doorNotifyService.notifyFaceUnlockStarted();
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "cmd", "/k",
                    "C:\\Users\\Selvendran\\AppData\\Local\\Programs\\Python\\Python313\\python.exe",
                    "C:\\Users\\Selvendran\\OneDrive\\Desktop\\FaceProject\\unlock.py"
            );
            pb.start();
            return "Face Recognition Started";
        } catch (IOException e) {
            e.printStackTrace();
            return "Error starting face recognition";
        }
    }

    @PostMapping("/door/unlock-pin")
    public String unlockDoorWithPin(@RequestBody(required = false) Map<String, Object> body) {
        String profileName = null;
        if (body != null && body.get("profileName") != null) {
            profileName = String.valueOf(body.get("profileName"));
        }
        doorNotifyService.notifyPinUnlock(profileName);
        return "Success";
    }

    @PostMapping("/door/unlock-pin-failed")
    public String unlockDoorWithPinFailed(@RequestBody(required = false) Map<String, Object> body) {
        String profileName = null;
        if (body != null && body.get("profileName") != null) {
            profileName = String.valueOf(body.get("profileName"));
        }
        doorNotifyService.notifyPinUnlockFailed(profileName);
        return "Failure Recorded";
    }
}
