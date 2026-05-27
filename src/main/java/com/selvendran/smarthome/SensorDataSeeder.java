package com.selvendran.smarthome;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class SensorDataSeeder implements CommandLineRunner {

    private final SensorDataRepository sensorDataRepository;

    public SensorDataSeeder(SensorDataRepository sensorDataRepository) {
        this.sensorDataRepository = sensorDataRepository;
    }

    @Override
    public void run(String... args) {
        if (sensorDataRepository.count() > 0) {
            return;
        }

        SensorData sample = new SensorData();
        sample.setTemperature(28.5);
        sample.setHumidity(62.0);
        sample.setMq7Gas(120);
        sample.setFlameDetected(false);
        sample.setMotionDetected(true);
        sample.setMq2Smoke(15);
        sample.setLdrValue(450);
        sample.setWaterLevel(0);
        sensorDataRepository.save(sample);
        System.out.println("Seeded demo sensor reading.");
    }
}
