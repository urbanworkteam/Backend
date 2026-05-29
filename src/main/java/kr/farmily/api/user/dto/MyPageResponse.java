package kr.farmily.api.user.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record MyPageResponse(
        Account account,
        CropsSummary crops,
        FarmLocationsSummary farmLocations,
        SubscriptionSummary subscription
) {

    public record Account(String name, String phone, String email) {}

    public record CropsSummary(long count, List<String> preview) {}

    public record FarmLocationsSummary(long count) {}

    public record SubscriptionSummary(String plan, int creditsUsed, int creditsLimit, OffsetDateTime resetAt) {}
}
