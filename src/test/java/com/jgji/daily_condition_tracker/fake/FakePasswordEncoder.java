package com.jgji.daily_condition_tracker.fake;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class FakePasswordEncoder implements PasswordEncoder {

    private static final int SALT_LENGTH = 32;
    private static final int ITERATIONS = 100000;

    private final String pepper; // 애플리케이션별 고정 시크릿
    private final SecureRandom secureRandom = new SecureRandom();

    public FakePasswordEncoder(String pepper) {
        this.pepper = pepper;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        try {
            // 1. 랜덤 Salt 생성
            byte[] salt = generateSalt();

            // 2. Key Stretching을 적용한 해시 생성
            byte[] hashedPassword = hashWithSalt(rawPassword.toString(), salt);

            // 3. Salt + Hash를 Base64로 인코딩하여 저장
            // 형식: {salt}${hash}
            String encodedSalt = Base64.getEncoder().encodeToString(salt);
            String encodedHash = Base64.getEncoder().encodeToString(hashedPassword);

            return encodedSalt + "$" + encodedHash;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-512 알고리즘을 찾을 수 없습니다.", e);
        }
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        try {
            // 저장된 패스워드에서 Salt와 Hash 분리
            String[] parts = encodedPassword.split("\\$");
            if (parts.length != 2) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedHash = Base64.getDecoder().decode(parts[1]);

            // 입력된 패스워드를 같은 Salt로 해시
            byte[] inputHash = hashWithSalt(rawPassword.toString(), salt);

            // 해시 비교 (타이밍 공격 방지를 위한 상수 시간 비교)
            return MessageDigest.isEqual(storedHash, inputHash);

        } catch (Exception e) {
            return false;
        }
    }

    private byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return salt;
    }

    /**
     * Salt와 Pepper를 사용한 Key Stretching 해시
     */
    private byte[] hashWithSalt(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");

        // 초기 해시: password + salt + pepper
        digest.update(password.getBytes());
        digest.update(salt);
        digest.update(pepper.getBytes());
        byte[] hash = digest.digest();

        // Key Stretching: 지정된 횟수만큼 해시 반복
        for (int i = 1; i < ITERATIONS; i++) {
            digest.reset();
            digest.update(hash);
            digest.update(salt); // 매 반복마다 Salt 추가
            hash = digest.digest();
        }

        return hash;
    }
}
