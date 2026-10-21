package com.example.event;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByUserId(Long userId);
    long countByEventId(Long eventId);
    boolean existsByEventIdAndUserId(Long eventId, Long userId);
}
