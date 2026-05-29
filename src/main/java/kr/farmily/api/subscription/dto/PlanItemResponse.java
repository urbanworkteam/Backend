package kr.farmily.api.subscription.dto;

import java.util.List;

public record PlanItemResponse(
        String code,
        String name,
        int price,
        String period,
        Integer creditsLimit,
        List<String> features,
        boolean disabled,
        boolean recommended,
        boolean comingSoon
) {}
