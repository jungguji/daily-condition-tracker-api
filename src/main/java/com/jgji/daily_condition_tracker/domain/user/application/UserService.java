package com.jgji.daily_condition_tracker.domain.user.application;

import com.jgji.daily_condition_tracker.domain.user.domain.User;
import com.jgji.daily_condition_tracker.domain.user.presentation.dto.CurrentUserResponse;
import com.jgji.daily_condition_tracker.global.exception.UserDeletedException;
import com.jgji.daily_condition_tracker.global.exception.UserNotActiveException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUserInfo(User user) {
        assert user != null : "사용자 정보는 null이 될 수 없습니다.";

        validateUserStatus(user);
        
        return CurrentUserResponse.from(user);
    }

    private void validateUserStatus(User user) {
        if (!user.isActive()) {
            throw new UserNotActiveException("비활성화된 계정입니다.");
        }
        
        if (user.isDeleted()) {
            throw new UserDeletedException("삭제된 계정입니다.");
        }
    }
} 