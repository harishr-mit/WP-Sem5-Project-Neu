package com.example.pass;

import jakarta.persistence.*;

@Entity
public class Pass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long registrationId;
    @Column(unique = true)
    public String token;
}
