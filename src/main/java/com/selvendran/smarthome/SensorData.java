package com.selvendran.smarthome;

import jakarta.persistence.*;

@Entity
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double temperature;
    private double humidity;

    private boolean motionDetected;
    private boolean flameDetected;

    private int mq2Smoke;
    private int mq6Lpg;

    private int ldrValue;
    private int soilMoisture;
    private int waterLevel;

    private int mq7Gas;
    private int ultraSonic;

    public SensorData() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public boolean isMotionDetected() {
        return motionDetected;
    }

    public void setMotionDetected(boolean motionDetected) {
        this.motionDetected = motionDetected;
    }

    public boolean isFlameDetected() {
        return flameDetected;
    }

    public void setFlameDetected(boolean flameDetected) {
        this.flameDetected = flameDetected;
    }

    public int getMq2Smoke() {
        return mq2Smoke;
    }

    public void setMq2Smoke(int mq2Smoke) {
        this.mq2Smoke = mq2Smoke;
    }

    public int getMq6Lpg() {
        return mq6Lpg;
    }

    public void setMq6Lpg(int mq6Lpg) {
        this.mq6Lpg = mq6Lpg;
    }

    public int getLdrValue() {
        return ldrValue;
    }

    public void setLdrValue(int ldrValue) {
        this.ldrValue = ldrValue;
    }

    public int getSoilMoisture() {
        return soilMoisture;
    }

    public void setSoilMoisture(int soilMoisture) {
        this.soilMoisture = soilMoisture;
    }

    public int getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(int waterLevel) {
        this.waterLevel = waterLevel;
    }

    public int getMq7Gas() {
        return mq7Gas;
    }

    public void setMq7Gas(int mq7Gas) {
        this.mq7Gas = mq7Gas;
    }

    public int getUltraSonic() {
        return ultraSonic;
    }

    public void setUltraSonic(int ultraSonic) {
        this.ultraSonic = ultraSonic;
    }
}