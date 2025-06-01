package com.jgji.daily_condition_tracker.domain.shared.infrastructure;

import com.jgji.daily_condition_tracker.domain.shared.value.EmailProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
class EmailSenderImpl implements EmailSender {
    
    private final JavaMailSender javaMailSender;
    private final EmailProperties emailProperties;

    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailProperties.getFrom());
            message.setTo("toEmail");
            message.setSubject("비밀번호 재설정 요청");
            
            String resetUrl = emailProperties.getFrontEndUrl() + "/reset-password?token=" + token;
            String text = "안녕하세요,\n\n" +
                    "비밀번호 재설정을 요청하셨습니다.\n" +
                    "아래 링크를 클릭하여 비밀번호를 재설정하세요:\n\n" +
                    resetUrl + "\n\n" +
                    "이 링크는 1시간 동안 유효합니다.\n" +
                    "만약 비밀번호 재설정을 요청하지 않으셨다면, 이 이메일을 무시하세요.\n\n" +
                    "감사합니다.";
            
            message.setText(text);
            javaMailSender.send(message);
            
            log.info("비밀번호 재설정 이메일이 성공적으로 발송되었습니다. 수신자: {}", toEmail);
        } catch (Exception e) {
            log.error("비밀번호 재설정 이메일 발송 실패. 수신자: {}, 오류: {}", toEmail, e.getMessage(), e);
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }
} 