package com.jgji.daily_condition_tracker.global.exception;

public class UserNotActiveException extends BusinessRuleViolationException {
    
    public UserNotActiveException(String message) {
        super(message);
    }
    
    public UserNotActiveException(String message, Throwable cause) {
        super(message, cause);
    }
} 