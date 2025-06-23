package com.abell.blog.controller;

import com.abell.blog.config.jwt.TokenProvider;
import com.abell.blog.dto.CreateAccessTokenRequest;
import com.abell.blog.dto.CreateAccessTokenResponse;
import com.abell.blog.service.RefreshTokenService;
import com.abell.blog.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class TokenApiController {

    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final TokenProvider tokenProvider;

    @PostMapping("/api/token")
    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(@RequestBody CreateAccessTokenRequest request){
        String newAccessToken = tokenService.createNewAccessToken(request.getRefreshToken());

        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateAccessTokenResponse(newAccessToken));
    }

    @DeleteMapping("/api/refresh-token")
    public ResponseEntity deleteRefreshToken() {
        refreshTokenService.delete();

        return ResponseEntity.ok()
                .build();
    }

}
