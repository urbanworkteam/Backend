package kr.farmily.api.subscription.dto;

import java.time.OffsetDateTime;

public record CreditStatus(
        String plan,
        int creditsRemaining,
        int creditsLimit,
        int creditsUsed,
        OffsetDateTime resetAt
) {}
