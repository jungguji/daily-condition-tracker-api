package com.jgji.daily_condition_tracker.domain.user.infrastructure;

import com.jgji.daily_condition_tracker.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
class UserRepositoryImpl implements UserRepository {
    
    private final UserJpaRepository userJPARepository;
    
    @Override
    public boolean existsByEmail(String email) {
        return userJPARepository.existsByEmail(email);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        return userJPARepository.findByEmail(email)
                .map(UserEntity::toDomain);
    }
    
    @Override
    public Optional<User> findByUserId(Long userId) {
        return userJPARepository.findById(userId)
                .map(UserEntity::toDomain);
    }
    
    @Override
    public User create(User user) {
        UserEntity userEntity = UserEntity.fromDomain(user);
        UserEntity savedEntity = userJPARepository.save(userEntity);
        return savedEntity.toDomain();
    }
    
    @Override
    public User update(User user) {
        UserEntity existingEntity = userJPARepository.findById(user.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + user.getUserId()));
        
        existingEntity.updateFromDomain(user);
        UserEntity savedEntity = userJPARepository.save(existingEntity);
        return savedEntity.toDomain();
    }
}
