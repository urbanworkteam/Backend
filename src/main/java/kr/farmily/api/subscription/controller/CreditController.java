package kr.farmily.api.subscription.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.farmily.api.common.response.ApiResponse;
import kr.farmily.api.common.security.CurrentUser;
import kr.farmily.api.subscription.dto.CreditStatus;
import kr.farmily.api.subscription.service.CreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Credits")
@RestController
@RequestMapping("/api/v1/credits")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;

    @GetMapping
    @Operation(summary = "남은 크레딧 조회")
    public ApiResponse<CreditStatus> get(CurrentUser user) {
        return ApiResponse.ok(creditService.getStatus(user.id()));
    }
}
