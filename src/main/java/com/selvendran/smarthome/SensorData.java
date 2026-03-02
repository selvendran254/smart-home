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

    public SensorData() {}

    // GETTERS & SETTERS BELOW
}