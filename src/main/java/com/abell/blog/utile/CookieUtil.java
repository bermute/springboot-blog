package com.abell.blog.utile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.SerializationUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CookieUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper(); //쿠키 역직렬화에 사용
    // 요청값(이름,값,만료 기간)을 바탕으로 쿠키 추가
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/"); // 이쿠키를 모든 경로 요청에 자동포함
        cookie.setMaxAge(maxAge);

        response.addCookie(cookie); // 인스턴스에 쿠키 추가 // 함정으로 메서드 이름이 다른데 다른메서드이다. 주의
    }

    //쿠키의 이름을 입력받아 쿠키 삭제
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies(); //getCookies() 가 반환이 쿠키 배열임

        if (cookies == null) {
            return;
        }

        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }

/*    // 객체를 JSON으로 직렬화해 Base64로 인코딩 (쿠키에 저장)
    public static String serialize(Object obj) {
        try {
            String json = objectMapper.writeValueAsString(obj); // Object → JSON 문자열
            return Base64.getUrlEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8)); // JSON → Base64
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize cookie object", e);
        }
    }

    // 쿠키 문자열(Base64)을 디코딩해 JSON → 객체로 역직렬화 //문제 있음 json 타입은 쿠키 용량을 못지킬수 있을 뿐더러 직렬화부터 어떤 OAuth2AuthorizationRequest 필드를 휘저어야 하기때문에 번거로움
    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(cookie.getValue()); // Base64 → JSON
            String json = new String(decoded, StandardCharsets.UTF_8);
            return objectMapper.readValue(json, cls); // JSON → Object
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize cookie", e);
        }
    }*/

    // 객체를 직렬화해 쿠키의 값으로 변환
    public static String serialize(Object obj) {
        return Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(obj)); //OAuth2AuthorizationRequest 객체 → Java 직렬화(byte[]) → Base64 → 쿠키 문자열 저장
    }

    // 역직렬화만 변경하는 방식으로 전환
    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(cookie.getValue());

            Object obj = SerializationUtils.deserialize(decoded);

            // 타입 체크를 명확히 해줌 (조작된 객체 방지)
            if (!cls.isInstance(obj)) {
                // 역직렬화된 객체가 의도한 타입이 아닐 경우 즉시 차단
                // 악의적인 객체가 들어와도 바로 예외 발생 후 중단
                // 코드 흐름상 타입 오용을 미리 막아줌
                throw new IllegalArgumentException("Deserialized object is not of expected type: " + cls.getName());
            }


            return cls.cast(obj);

        } catch (Exception e) {
            throw new RuntimeException("Failed to safely deserialize cookie", e);
        }
    }



/*    // 쿠키 문자열(Base64)을 디코딩해 JSON → 객체로 역직렬화
    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        return cls.cast(
                SerializationUtils.deserialize(
                        Base64.getUrlDecoder().decode(cookie.getValue())
                )
        );
    }*/
}