package kr.farmily.api.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.farmily.api.auth.dto.WithdrawRequest;
import kr.farmily.api.auth.dto.WithdrawResponse;
import kr.farmily.api.auth.service.WithdrawService;
import kr.farmily.api.common.response.ApiResponse;
import kr.farmily.api.common.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth - Withdraw")
@RestController
@RequestMapping("/api/v1/auth/withdraw")
@RequiredArgsConstructor
public class WithdrawController {

    private final WithdrawService withdrawService;

    @PostMapping
    @Operation(summary = "회원 탈퇴 (30일 유예)")
    public ApiResponse<WithdrawResponse> withdraw(CurrentUser user, @RequestBody(required = false) WithdrawRequest req) {
        String reason = req != null ? req.reason() : null;
        return ApiResponse.ok(new WithdrawResponse(withdrawService.withdraw(user.id(), reason)));
    }

    @PostMapping("/cancel")
    @Operation(summary = "탈퇴 취소 (30일 이내)")
    public ApiResponse<Void> cancel(CurrentUser user) {
        withdrawService.cancelWithdraw(user.id());
        return ApiResponse.ok();
    }
}
