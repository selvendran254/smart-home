package com.selvendran.smarthome;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;     // Light 1, Fan 2
    private String type;     // LIGHT, FAN
    private boolean status;  // ON / OFF

    @ManyToOne
    @JoinColumn(name = "room_id")
    @JsonIgnore   // Prevent infinite JSON loop
    private Room room;

    public Device() {}

    public Device(String name, String type) {
        this.name = name;
        this.type = type;
        this.status = false;
    }

    // ---------------- GETTERS & SETTERS ----------------

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}