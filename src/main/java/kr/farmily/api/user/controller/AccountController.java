package kr.farmily.api.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.farmily.api.common.response.ApiResponse;
import kr.farmily.api.common.security.CurrentUser;
import kr.farmily.api.user.dto.AccountUpdateRequest;
import kr.farmily.api.user.dto.MyPageResponse;
import kr.farmily.api.user.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Account")
@RestController
@RequestMapping("/api/v1/mypage/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService service;

    @PatchMapping
    @Operation(summary = "내 계정 정보 수정 (이름·이메일)")
    public ApiResponse<MyPageResponse.Account> update(CurrentUser user,
                                                      @Valid @RequestBody AccountUpdateRequest req) {
        return ApiResponse.ok(service.update(user.id(), req));
    }
}
