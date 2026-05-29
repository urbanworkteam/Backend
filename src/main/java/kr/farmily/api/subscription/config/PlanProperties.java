package kr.farmily.api.subscription.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "farmily.plans")
public record PlanProperties(
        PlanLimit free,
        PlanLimit allInOne
) {

    public record PlanLimit(int creditsLimit, String period, Integer price) {}

    public int limitOf(String plan) {
        return switch (plan) {
            case "ALL_IN_ONE" -> allInOne != null ? allInOne.creditsLimit() : 50;
            default -> free != null ? free.creditsLimit() : 5;
        };
    }

    public int priceOf(String plan) {
        return switch (plan) {
            case "ALL_IN_ONE" -> allInOne != null && allInOne.price() != null ? allInOne.price() : 14900;
            default -> 0;
        };
    }
}
