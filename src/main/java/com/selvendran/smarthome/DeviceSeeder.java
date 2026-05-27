package com.selvendran.smarthome;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DeviceSeeder implements CommandLineRunner {

    private static final Map<String, String[][]> ROOM_DEVICE_TEMPLATES = buildTemplates();

    private final DeviceRepository deviceRepository;
    private final RoomRepository roomRepository;

    public DeviceSeeder(DeviceRepository deviceRepository, RoomRepository roomRepository) {
        this.deviceRepository = deviceRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public void run(String... args) {
        List<Room> rooms = roomRepository.findAll();
        if (rooms.isEmpty()) {
            return;
        }

        int added = 0;
        for (Room room : rooms) {
            added += ensureRoomDevices(room);
        }
        if (added > 0) {
            System.out.println("Ensured smart-home devices: added " + added + " new device(s).");
        }
    }

    private int ensureRoomDevices(Room room) {
        String[][] template = ROOM_DEVICE_TEMPLATES.get(room.getName());
        if (template == null) {
            template = new String[][]{
                    {"Room Light", "LIGHT"},
                    {"Room Fan", "FAN"}
            };
        }

        List<Device> existing = deviceRepository.findByRoom_Id(room.getId());
        Set<String> names = new HashSet<>();
        for (Device d : existing) {
            names.add(d.getName().toLowerCase(Locale.ROOT));
        }

        int added = 0;
        for (String[] def : template) {
            if (!names.contains(def[0].toLowerCase(Locale.ROOT))) {
                Device device = new Device(def[0], def[1]);
                device.setRoom(room);
                device.setStatus(false);
                deviceRepository.save(device);
                added++;
            }
        }
        return added;
    }

    private static Map<String, String[][]> buildTemplates() {
        Map<String, String[][]> map = new LinkedHashMap<>();
        map.put("Living Room", new String[][]{
                {"Main Light", "LIGHT"},
                {"Ceiling Fan", "FAN"},
                {"Smart TV", "TV"},
                {"Air Conditioner", "AC"},
                {"Curtain", "CURTAIN"}
        });
        map.put("Bedroom 1", new String[][]{
                {"Bedroom Light", "LIGHT"},
                {"Ceiling Fan", "FAN"},
                {"Air Conditioner", "AC"},
                {"Night Lamp", "LAMP"}
        });
        map.put("Bedroom 2", new String[][]{
                {"Bedroom Light", "LIGHT"},
                {"Ceiling Fan", "FAN"},
                {"Air Conditioner", "AC"},
                {"Night Lamp", "LAMP"}
        });
        map.put("Kitchen", new String[][]{
                {"Kitchen Light", "LIGHT"},
                {"Exhaust Fan", "EXHAUST"},
                {"Chimney", "APPLIANCE"},
                {"Water Purifier", "APPLIANCE"}
        });
        map.put("Bathroom", new String[][]{
                {"Bathroom Light", "LIGHT"},
                {"Exhaust Fan", "EXHAUST"},
                {"Geyser", "GEYSER"},
                {"Floor Heater", "HEATER"}
        });
        map.put("Hall", new String[][]{
                {"Hall Light", "LIGHT"},
                {"Ceiling Fan", "FAN"},
                {"Main Door Lock", "DOOR"},
                {"Door Bell", "BELL"}
        });
        return map;
    }
}
