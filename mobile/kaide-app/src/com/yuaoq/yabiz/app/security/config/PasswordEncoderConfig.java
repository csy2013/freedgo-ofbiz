package com.yuaoq.yabiz.app.security.config;

import com.yuaoq.yabiz.app.security.auth.TokenPasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * PasswordEncoderConfig
 *
 * @author vladimir.stankovic
 *
 * Dec 27, 2016
 */
@Configuration
public class PasswordEncoderConfig {
    @Bean
    protected TokenPasswordEncoder passwordEncoder() {
        return new TokenPasswordEncoder();
    }
}
