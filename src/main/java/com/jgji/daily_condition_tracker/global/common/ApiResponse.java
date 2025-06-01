package com.jgji.daily_condition_tracker.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer code; // 커스텀 코드
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String status; // 성공/실패 등 상태 (예: "SUCCESS", "FAILURE")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message; // 결과 메시지
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data; // 실제 응답 데이터

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .status("SUCCESS")
                .message("요청이 성공적으로 처리되었습니다.")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .status("FAILURE")
                .message(message)
                .build();
    }
} 