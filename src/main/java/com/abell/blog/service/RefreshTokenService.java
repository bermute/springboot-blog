package com.abell.blog.service;

import com.abell.blog.config.jwt.TokenProvider;
import com.abell.blog.domain.RefreshToken;
import com.abell.blog.domain.User;
import com.abell.blog.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class RefreshTokenService {
    Logger log = LoggerFactory.getLogger(getClass());
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;

    public RefreshToken findByRefreshToken(String refreshToken){
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(()->new IllegalArgumentException("Unexpected token"));
    }

    @Transactional
    public void delete() {
        String token = SecurityContextHolder.getContext().getAuthentication().getCredentials().toString();
        /* SecurityContextHolder 현재 스레드의 보안 컨텍스트를 담고 있는 전역 저장소
        * getContext() 현재 요청(스레드)의 SecurityContext 반환
        * getAuthentication() 	현재 로그인한 사용자(인증 객체)를 반환
        * getCredentials() 인증 객체에 들어 있는  "토큰"  자격 증명 정보
        *  toString() 그것을 문자열로 변환
        * */

        Long userId = tokenProvider.getUserId(token);
        log.info("userId 겟로거: {}", userId);
        log.info("userId 겟로거2: {}", SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        refreshTokenRepository.deleteByUserId(userId);
    }
}
