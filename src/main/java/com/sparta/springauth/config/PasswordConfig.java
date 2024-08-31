package com.sparta.springauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
//빈으로 저장될 때 passwordConfig 로 저장
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() { //passwordEncoder
        return new BCryptPasswordEncoder();
        // PasswordEncoder는 인터페이스 , BCryptPasswordEncoder는 구현체
        // 비밀버호를 암호화 해주는 해시함수
    }
}