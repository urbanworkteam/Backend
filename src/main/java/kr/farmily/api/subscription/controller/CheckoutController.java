package kr.farmily.api.subscription.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.farmily.api.common.response.ApiResponse;
import kr.farmily.api.common.security.CurrentUser;
import kr.farmily.api.subscription.dto.CheckoutConfirmRequest;
import kr.farmily.api.subscription.dto.CheckoutStartRequest;
import kr.farmily.api.subscription.dto.CheckoutStartResponse;
import kr.farmily.api.subscription.dto.SubscriptionResponse;
import kr.farmily.api.subscription.service.CheckoutService;
import kr.farmily.api.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Checkout (PortOne)")
@RestController
@RequestMapping("/api/v1/subscription/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final SubscriptionService subscriptionService;

    @PostMapping
    @Operation(summary = "결제 시작")
    public ApiResponse<CheckoutStartResponse> start(CurrentUser user, @Valid @RequestBody CheckoutStartRequest req) {
        return ApiResponse.ok(checkoutService.start(user.id(), req));
    }

    @PostMapping("/{checkoutId}/confirm")
    @Operation(summary = "결제 검증/확정")
    public ApiResponse<SubscriptionResponse> confirm(CurrentUser user, @PathVariable Long checkoutId,
                                                     @Valid @RequestBody CheckoutConfirmRequest req) {
        checkoutService.confirm(user.id(), checkoutId, req.impUid(), req.merchantUid());
        return ApiResponse.ok(subscriptionService.getMine(user.id()));
    }
}
