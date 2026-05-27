package com.selvendran.smarthome.repository;

import com.selvendran.smarthome.entity.DeviceData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceDataRepository extends JpaRepository<DeviceData, Long> {
}
