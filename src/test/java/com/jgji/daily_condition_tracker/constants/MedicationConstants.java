package com.jgji.daily_condition_tracker.constants;

public class MedicationConstants {

    public static final long DEFAULT_USER_ID = 1L;
    public static final String DEFAULT_NAME = "타이레놀";
    public static final Integer DEFAULT_DOSAGE = 500;
    public static final String DEFAULT_UNIT = "mg";
    public static final String DEFAULT_DESCRIPTION = "두통, 발열시 복용";
    public static final boolean DEFAULT_IS_ACTIVE = true;

    public static final String VALID_NAME_255_CHARS = "a".repeat(255);
    public static final String INVALID_NAME_256_CHARS = "a".repeat(256);

} 