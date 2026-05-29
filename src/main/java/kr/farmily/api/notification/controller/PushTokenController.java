package kr.farmily.api.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.farmily.api.common.response.ApiResponse;
import kr.farmily.api.common.security.CurrentUser;
import kr.farmily.api.notification.dto.DeleteTokenRequest;
import kr.farmily.api.notification.dto.RegisterTokenRequest;
import kr.farmily.api.notification.service.PushTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Push Tokens")
@RestController
@RequestMapping("/api/v1/push-tokens")
@RequiredArgsConstructor
public class PushTokenController {

    private final PushTokenService service;

    @PostMapping
    @Operation(summary = "FCM 토큰 등록 (UPSERT)")
    public ApiResponse<Void> register(CurrentUser user, @Valid @RequestBody RegisterTokenRequest req) {
        service.register(user.id(), req.platform(), req.token());
        return ApiResponse.ok();
    }

    @DeleteMapping
    @Operation(summary = "FCM 토큰 삭제 (멱등)")
    public ApiResponse<Void> delete(@Valid @RequestBody DeleteTokenRequest req) {
        service.delete(req.token());
        return ApiResponse.ok();
    }
}
