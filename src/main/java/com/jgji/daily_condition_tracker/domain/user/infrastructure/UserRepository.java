package com.jgji.daily_condition_tracker.domain.user.infrastructure;

import com.jgji.daily_condition_tracker.domain.user.domain.User;
import java.util.Optional;

public interface UserRepository {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    User create(User user);

    User update(User user);
}
