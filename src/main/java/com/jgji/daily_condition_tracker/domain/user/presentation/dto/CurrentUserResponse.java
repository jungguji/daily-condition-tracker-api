
package com.jgji.daily_condition_tracker.domain.user.presentation.dto;

import com.jgji.daily_condition_tracker.domain.user.domain.User;

public record CurrentUserResponse(
    long userId,
    String email,
    String nickname,
    String socialProvider,
    boolean isSuperuser
) {
    public static CurrentUserResponse from(User user) {
        return new CurrentUserResponse(
            user.getUserId(),
            user.getEmail().getValue(),
            user.getNickname(),
            user.getSocialProvider(),
            user.isSuperuser()
        );
    }
} 