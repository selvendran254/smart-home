package com.selvendran.smarthome;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class RoomSeeder implements CommandLineRunner {

    private final RoomRepository roomRepository;

    public RoomSeeder(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public void run(String... args) {
        if (roomRepository.count() > 0) {
            return;
        }

        roomRepository.save(new Room("Living Room", "living.jpg"));
        roomRepository.save(new Room("Bedroom", "bedroom.jpg"));
        roomRepository.save(new Room("Kitchen", "kitchen.jpg"));
        roomRepository.save(new Room("Bathroom", "bathroom.jpg"));
        roomRepository.save(new Room("Hall", "hall.jpg"));
        System.out.println("Seeded default rooms for demo.");
    }
}
