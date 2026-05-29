package kr.farmily.api.subscription.dto;

import java.time.OffsetDateTime;

public record SubscriptionResponse(
        String plan,
        String status,
        OffsetDateTime currentPeriodEnd,
        boolean autoRenew,
        int creditsRemaining,
        int creditsUsed,
        int creditsLimit
) {}
