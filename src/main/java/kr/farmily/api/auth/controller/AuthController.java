package kr.farmily.api.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.farmily.api.auth.dto.AuthTokenResponse;
import kr.farmily.api.auth.dto.LogoutRequest;
import kr.farmily.api.auth.dto.RefreshRequest;
import kr.farmily.api.auth.service.RefreshTokenService;
import kr.farmily.api.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth - Token")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RefreshTokenService refreshService;

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신 (회전)")
    public ApiResponse<AuthTokenResponse> refresh(@Valid @RequestBody RefreshRequest req,
                                                  HttpServletRequest httpReq) {
        return ApiResponse.ok(refreshService.rotate(req.refreshToken(), httpReq));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest req) {
        refreshService.logout(req.refreshToken());
        return ApiResponse.ok();
    }
}
