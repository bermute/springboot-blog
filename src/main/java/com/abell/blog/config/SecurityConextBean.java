package com.abell.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Configuration
public class SecurityConextBean {
    //원래 WebOAuthSecurityConfig 에있었는데 순환참조 일어나서 따로 뺌
    @Bean // 이것만 따로 만들어서 빈 주입하면 유저 서비스에서 순환참조 안일어나긴함 아니면 유저 서비스에서 new 생성으로 대체하면됌
    public BCryptPasswordEncoder bCryptPasswordEncoder() { //비밀번호 암호화용
        return new BCryptPasswordEncoder();
    }

    // 이것도 따로 뺌
    @Bean
    public SecurityContextLogoutHandler securityContextLogoutHandler() {
        return new SecurityContextLogoutHandler();
    }
}
