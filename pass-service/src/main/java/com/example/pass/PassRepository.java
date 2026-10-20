package com.example.pass;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PassRepository extends JpaRepository<Pass, Long> {
    Pass findByToken(String token);
}
