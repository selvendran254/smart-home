package com.selvendran.smarthome;

import com.selvendran.smarthome.entity.DeviceData;
import com.selvendran.smarthome.repository.DeviceDataRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DeviceDataSeeder implements CommandLineRunner {

    private final DeviceDataRepository deviceDataRepository;

    public DeviceDataSeeder(DeviceDataRepository deviceDataRepository) {
        this.deviceDataRepository = deviceDataRepository;
    }

    @Override
    public void run(String... args) {
        if (deviceDataRepository.count() == 0) {

            List<DeviceData> dataList = new ArrayList<>();
            String[] devices = {
                "Living Room AC", 
                "Kitchen Light", 
                "Bedroom Fan", 
                "Garage Door", 
                "Garden Sprinkler", 
                "Smart TV", 
                "Bathroom Heater", 
                "Dining Room Light", 
                "Hall Sensor", 
                "Main Door Lock"
            };

            Random random = new Random();

            for (int i = 0; i < 50; i++) {
                String device = devices[random.nextInt(devices.length)];
                String status = random.nextBoolean() ? "ON" : "OFF";
                
                // Temperature between 18 and 40
                Double temperature = 18.0 + (random.nextDouble() * (40.0 - 18.0));
                
                // Humidity between 30 and 90
                Double humidity = 30.0 + (random.nextDouble() * (90.0 - 30.0));

                // Random timestamp within the last 7 days (7 days = 10080 minutes)
                LocalDateTime timestamp = LocalDateTime.now().minusMinutes(random.nextInt(10080));

                DeviceData deviceData = new DeviceData(
                        null,
                        device,
                        status,
                        Math.round(temperature * 10.0) / 10.0,
                        Math.round(humidity * 10.0) / 10.0,
                        timestamp
                );
                
                dataList.add(deviceData);
            }

            deviceDataRepository.saveAll(dataList);
            System.out.println("Successfully seeded 50 rows of device data!");
        }
    }
}
