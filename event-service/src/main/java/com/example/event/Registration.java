package com.example.event;

import jakarta.persistence.*;

@Entity
public class Registration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long eventId;
    public Long userId;
    public String passToken;
}
