package com.example.user;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String passToken;
    public Long registrationId;
    public Long scannedBy;
    public Long userId;
    public Long eventId;
    public String eventName;
    public String username;
    public LocalDateTime scannedAt;
}
