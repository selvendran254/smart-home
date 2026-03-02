package com.selvendran.smarthome;

import jakarta.persistence.*;

@Entity
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String image;

    private boolean lightOn;

    public Room() {}

    public Room(String name, String image) {
        this.name = name;
        this.image = image;
        this.lightOn = false;
    }

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public boolean isLightOn() { return lightOn; }
    public void setLightOn(boolean lightOn) { this.lightOn = lightOn; }
}