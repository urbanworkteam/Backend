package kr.farmily.api.subscription.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.farmily.api.common.response.ApiResponse;
import kr.farmily.api.subscription.service.PaymentWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Payment Webhook (PortOne)")
@RestController
@RequestMapping("/api/v1/webhooks/payments")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentWebhookService service;

    @PostMapping
    @Operation(summary = "포트원 결제 웹훅")
    public ApiResponse<Map<String, Boolean>> webhook(@RequestBody Map<String, Object> body) {
        String impUid = (String) body.get("imp_uid");
        String merchantUid = (String) body.get("merchant_uid");
        String status = (String) body.get("status");
        service.process(impUid, merchantUid, status);
        return ApiResponse.ok(Map.of("ok", true));
    }
}
