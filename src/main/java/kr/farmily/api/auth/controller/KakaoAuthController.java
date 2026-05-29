package kr.farmily.api.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.farmily.api.auth.dto.AuthTokenResponse;
import kr.farmily.api.auth.dto.KakaoLoginRequest;
import kr.farmily.api.auth.service.AuthService;
import kr.farmily.api.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth - Kakao")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class KakaoAuthController {

    private final AuthService authService;

    @PostMapping("/kakao")
    @Operation(summary = "카카오 OAuth 로그인", description = "카카오 authorization code 로 JWT 발급")
    public ApiResponse<AuthTokenResponse> kakaoLogin(@Valid @RequestBody KakaoLoginRequest req,
                                                     HttpServletRequest httpReq) {
        return ApiResponse.ok(authService.loginWithKakao(req, httpReq));
    }
}
