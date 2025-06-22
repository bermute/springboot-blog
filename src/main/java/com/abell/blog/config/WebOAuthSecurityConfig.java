package com.abell.blog.config;

import com.abell.blog.config.jwt.TokenProvider;
import com.abell.blog.config.oauth.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.abell.blog.config.oauth.OAuth2SuccessHandler;
import com.abell.blog.config.oauth.OAuth2UserCustomService;
import com.abell.blog.repository.RefreshTokenRepository;
import com.abell.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;


@RequiredArgsConstructor
@Configuration
public class WebOAuthSecurityConfig {

    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;

    @Bean
    public WebSecurityCustomizer configure(){ //스프링 시큐리티 기능 비활성화
        return (web) -> web.ignoring()   //정적 파일(img, css, js)과 H2 콘솔 접근 시 Spring Security 필터 적용을 비활성화.
                .requestMatchers(toH2Console())
                .requestMatchers(
                        new AntPathRequestMatcher("/static/img/**"),
                        new AntPathRequestMatcher("/css/**"),
                        new AntPathRequestMatcher("/js/**")
                );
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // REST API 방식과 JWT 사용을 위해 불필요한 로그인 관련 설정 제거 + 세션 사용안함
                .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class) //JWT 토큰을 파싱해 인증 처리를 수행하는 필터를 등록
                .authorizeHttpRequests(auth -> auth  // authorizeRequests 사용으로 변경요구 사항 뜨기때문에 변경
                        .requestMatchers(new AntPathRequestMatcher("/api/token")).permitAll() // /api/token: 누구나 접근 허용
                        .requestMatchers(new AntPathRequestMatcher("/api/**")).authenticated()
                        //.requestMatchers("/articles").authenticated() 이건좀더 생각해봐야될듯
                        // /api/**: 인증된 사용자만 접근 가능
                        .anyRequest().permitAll()) //나머지 경로: 모두 허용
                .oauth2Login(oauth2 -> oauth2 //소셜 로그인(OAuth2) 설정: 로그인 성공 시 JWT 발급
                        .loginPage("/login")
                        .authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint.authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository())) // 여기 까지가 로그인  요청 정보 저장 전략"을 설정하는 코드 쿠키로 저장 전략  // 로그인 인증 결과(code)가 돌아오기도 전의 작업
                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userService(oAuth2UserCustomService)) //받은 사용자 정보를 내가 만든 서비스(oAuth2UserCustomService)로 넘김
                        .successHandler(oAuth2SuccessHandler()) // → JWT 생성, 리프레시 토큰 저장, 응답 커스터마이징 등
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling //인증 실패 시 401 코드 반환
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**")
                        ))
                .build();
    }




    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() { //	OAuth2 로그인 성공 시 JWT 발급 및 리프레시 토큰 저장 처리
        return new OAuth2SuccessHandler(tokenProvider,
                refreshTokenRepository,
                oAuth2AuthorizationRequestBasedOnCookieRepository(),
                userService
        );
    }


    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() { //JWT 토큰 인증 필터
        return new TokenAuthenticationFilter(tokenProvider);
    }


    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() { //OAuth2 로그인 중 요청 정보 저장용 (쿠키 기반)
        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
    }


//    @Bean // 이것만 따로 만들어서 빈 주입하면 유저 서비스에서 순환참조 안일어나긴함 아니면 유저 서비스에서 new 생성으로 대체하면됌 대체로 진행
//    public BCryptPasswordEncoder bCryptPasswordEncoder() { //비밀번호 암호화용
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public SecurityContextLogoutHandler securityContextLogoutHandler() {
//        return new SecurityContextLogoutHandler();
//    }

}
