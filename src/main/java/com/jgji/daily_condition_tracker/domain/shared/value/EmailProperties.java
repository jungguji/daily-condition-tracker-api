package com.jgji.daily_condition_tracker.domain.shared.value;

public interface EmailProperties {

    /**
     * 이메일 발신자 주소를 반환합니다.
     */
    String getFrom();
    
    /**
     * 프론트엔드 URL을 반환합니다.
     */
    String getFrontEndUrl();
    
}
