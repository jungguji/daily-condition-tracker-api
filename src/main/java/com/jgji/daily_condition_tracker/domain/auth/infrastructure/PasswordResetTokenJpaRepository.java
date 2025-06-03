package com.jgji.daily_condition_tracker.domain.auth.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenEntity, Long> {
    
    Optional<PasswordResetTokenEntity> findByToken(String token);
} 