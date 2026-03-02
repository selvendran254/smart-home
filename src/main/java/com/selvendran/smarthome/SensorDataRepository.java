package com.selvendran.smarthome;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorDataRepository
        extends JpaRepository<SensorData, Long> {
}