package com.example.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    boolean existsByPassToken(String passToken);
}
