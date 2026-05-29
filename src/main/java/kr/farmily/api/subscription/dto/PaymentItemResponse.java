package kr.farmily.api.subscription.dto;

import java.time.OffsetDateTime;

public record PaymentItemResponse(
        Long id,
        String plan,
        int amount,
        String status,
        OffsetDateTime paidAt,
        String receiptUrl
) {}
