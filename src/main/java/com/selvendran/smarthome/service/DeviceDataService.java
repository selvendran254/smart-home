package com.selvendran.smarthome.service;

import com.selvendran.smarthome.entity.DeviceData;
import com.selvendran.smarthome.repository.DeviceDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeviceDataService {

    private final DeviceDataRepository deviceDataRepository;

    @Autowired
    public DeviceDataService(DeviceDataRepository deviceDataRepository) {
        this.deviceDataRepository = deviceDataRepository;
    }

    public DeviceData saveDeviceData(DeviceData data) {
        return deviceDataRepository.save(data);
    }

    public List<DeviceData> getAllDeviceData() {
        return deviceDataRepository.findAll();
    }

    public Optional<DeviceData> getDeviceDataById(Long id) {
        return deviceDataRepository.findById(id);
    }

    public boolean deleteDeviceData(Long id) {
        if (deviceDataRepository.existsById(id)) {
            deviceDataRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
