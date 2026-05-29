package kr.farmily.api.subscription.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.farmily.api.common.response.ApiResponse;
import kr.farmily.api.common.response.PageResponse;
import kr.farmily.api.common.security.CurrentUser;
import kr.farmily.api.subscription.dto.PaymentItemResponse;
import kr.farmily.api.subscription.dto.PlanItemResponse;
import kr.farmily.api.subscription.dto.SubscriptionResponse;
import kr.farmily.api.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "Subscription")
@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    @Operation(summary = "내 구독 조회")
    public ApiResponse<SubscriptionResponse> get(CurrentUser user) {
        return ApiResponse.ok(subscriptionService.getMine(user.id()));
    }

    @GetMapping("/payments")
    @Operation(summary = "결제 내역 (페이지네이션)")
    public ApiResponse<PageResponse<PaymentItemResponse>> payments(CurrentUser user,
                                                                   @RequestParam(required = false) String cursor,
                                                                   @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(subscriptionService.getPayments(user.id(), cursor, Math.min(50, Math.max(1, limit))));
    }

    @GetMapping("/plans")
    @Operation(summary = "플랜 목록")
    public ApiResponse<List<PlanItemResponse>> plans() {
        return ApiResponse.ok(subscriptionService.listPlans());
    }

    @PostMapping("/cancel")
    @Operation(summary = "구독 취소 (periodEnd 까지 유지)")
    public ApiResponse<Map<String, Object>> cancel(CurrentUser user) {
        OffsetDateTime validUntil = subscriptionService.cancel(user.id());
        return ApiResponse.ok(Map.of("status", "CANCELED", "validUntil", validUntil));
    }
}
