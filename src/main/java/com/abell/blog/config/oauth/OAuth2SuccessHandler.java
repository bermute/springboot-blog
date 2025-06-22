package com.abell.blog.config.oauth;

import com.abell.blog.config.jwt.TokenProvider;
import com.abell.blog.domain.RefreshToken;
import com.abell.blog.domain.User;
import com.abell.blog.repository.RefreshTokenRepository;
import com.abell.blog.service.UserService;
import com.abell.blog.utile.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.io.IOException;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);
    public static final String REDIRECT_PATH = "/articles";
    //public static final String REDIRECT_PATH = "/login";

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository;
    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException{
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        User user = userService.findByEmail((String) oAuth2User.getAttributes().get("email")); // 1. 로그인한 사용자 정보 추출 (OAuth2User → email) //
                                                                                            // 2. 사용자 정보로 User 엔티티 조회
        String refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION); // 3. Refresh Token 생성 및 저장
        saveRefreshToken(user.getId(), refreshToken);   //생성된 리프레시 토큰 서버에 저장 유저아이디와 함께  // 이미 존재하면 update() 메서드로 새 토큰으로 갱신, 없으면 새로 생성해서 저장.
        addRefreshTokenToCookie(request, response, refreshToken); // 5.  쿠키 초기화 및 새로운 쿠키 저장

        String accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION); // 4. Access Token 생성
        String targetUrl = getTargetUrl(accessToken); // 리다이렉트할 URL에 Access Token을 쿼리 파라미터로 붙여 생성 예시: /articles?token=ACCESS_TOKEN


        clearAuthenticationAttributes(request, response); // 6. 인증 관련 속성 클리어 // 인증 관련 쿠키 및 세션 정보 정리 // authorizationRequestRepository.removeAuthorizationRequestCookies()는 쿠키 기반 Authorization 요청 정보를 제거

        getRedirectStrategy().sendRedirect(request, response, targetUrl); // 7. Access Token을 쿼리 파라미터로 붙여서 리다이렉트
    }

    private void saveRefreshToken(Long userId, String newRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
                .map(entity -> entity.update(newRefreshToken))
                .orElse(new RefreshToken(userId, newRefreshToken));

        refreshTokenRepository.save(refreshToken);
    }

    private void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();

        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, cookieMaxAge);
    }

    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    private String getTargetUrl(String token) {
        return UriComponentsBuilder.fromUriString(REDIRECT_PATH)
                .queryParam("token", token)
                .build()
                .toUriString();
    }


}
