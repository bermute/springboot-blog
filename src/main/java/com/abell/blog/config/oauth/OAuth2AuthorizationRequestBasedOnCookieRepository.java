package com.abell.blog.config.oauth;

import com.abell.blog.utile.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.web.util.WebUtils;

public class OAuth2AuthorizationRequestBasedOnCookieRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    public final static String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";  //쿠키 이름을 정의 (oauth2_auth_request)
    private final static int COOKIE_EXPIRE_SECONDS = 18000; //쿠키 만료 시간 18000초 (5시간)

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest
                                                                         request, HttpServletResponse response) {
        return this.loadAuthorizationRequest(request); //인증 요청 정보를 꺼내오지만, 실제 쿠키 삭제는 안 함
                                                        //삭제는 removeAuthorizationRequestCookies() 메서드로 따로 분리되어 있음
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest
                                                                       request) {
        Cookie cookie = WebUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME); //요청에 포함된 쿠키에서 "oauth2_auth_request"라는 이름의 쿠키를 찾음
        return CookieUtil.deserialize(cookie, OAuth2AuthorizationRequest.class); // 찾은 쿠키를 역직렬화해서 OAuth2AuthorizationRequest 객체로 반환
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest
                                                 authorizationRequest, HttpServletRequest request, HttpServletResponse response) {

        if (authorizationRequest == null) {  // authorizationRequest가 null이면 → 쿠키 삭제
            removeAuthorizationRequestCookies(request, response);
            return;
        }
        CookieUtil.addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,  //authorizationRequest 객체를 직렬화해서 문자열로 바꿈
                CookieUtil.serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS); // 쿠키에 저장해서 응답에 실어 클라이언트에 보냄
    }

    public void removeAuthorizationRequestCookies(HttpServletRequest request,
                                                  HttpServletResponse response) {
        CookieUtil.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME); // 저장된 쿠키(oauth2_auth_request)를 삭제함
    }
}