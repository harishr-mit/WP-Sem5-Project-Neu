package com.example.event;

import jakarta.persistence.*;

@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String name;
    public String location;
    public String dateTime;
    public String description;
    public Integer capacity; // null = unlimited
}
