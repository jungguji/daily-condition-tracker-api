package com.jgji.daily_condition_tracker.global.exception;

public class UserDeletedException extends BusinessRuleViolationException {
    
    public UserDeletedException(String message) {
        super(message);
    }
    
    public UserDeletedException(String message, Throwable cause) {
        super(message, cause);
    }
} 