package com.selvendran.smarthome;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/rooms")
@CrossOrigin
public class RoomController {

    private final RoomRepository roomRepository;

    public RoomController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @GetMapping
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @PostMapping
    public Room createRoom(@RequestBody Room room) {
        return roomRepository.save(room);
    }

    @DeleteMapping("/{id}")
    public void deleteRoom(@PathVariable Long id) {
        roomRepository.deleteById(id);
    }
    @PutMapping("/{id}/light")
public Room updateLight(@PathVariable Long id,
                        @RequestParam boolean status) {

    Room room = roomRepository.findById(id).orElseThrow();
    room.setLightOn(status);
    return roomRepository.save(room);
}
}