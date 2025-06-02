package com.jgji.daily_condition_tracker.domain.user.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

interface UserJPARepository extends JpaRepository<UserEntity, Long> {
    
    boolean existsByEmail(String email);
    
    Optional<UserEntity> findByEmail(String email);
}
