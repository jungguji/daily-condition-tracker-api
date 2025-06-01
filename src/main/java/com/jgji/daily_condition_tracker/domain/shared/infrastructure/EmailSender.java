package com.jgji.daily_condition_tracker.domain.shared.infrastructure;

public interface EmailSender {
    void sendPasswordResetEmail(String toEmail, String token);
}
