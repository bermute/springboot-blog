package com.abell.blog.config.oauth;

import com.abell.blog.domain.User;
import com.abell.blog.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);  // 인가 서버에서 사용자 정보 받아오기
        saveOrUpdate(user); // 사용자 정보 저장 또는 갱신
        return user; // 인증 객체로 리턴 (SecurityContext 에 저장됨)
    }

    @Transactional
    private User saveOrUpdate(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes(); // 구글 로 받은 json 데이터를 map 형태로 넘김

        String email = (String) attributes.get("email"); //이메일 추출
        String name = (String) attributes.get("name"); //이름 추출

        User user = userRepository.findByEmail(email) // 옵셔널 반환
                .map(entity -> entity.update(name))  // 이미 있으면 이름만 업데이트 // 값이 존재하면
                .orElse(User.builder()              // 없으면 새로 등록
                        .email(email)
                        .nickname(name)
                        .build());

        return userRepository.save(user);
    }
}
