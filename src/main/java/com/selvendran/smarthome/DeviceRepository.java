package com.selvendran.smarthome;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    List<Device> findByRoomIsNull();
    List<Device> findByRoomId(Long roomId);

    

}