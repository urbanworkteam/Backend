package kr.farmily.api.subscription.dto;

public record CheckoutStartResponse(
        Long checkoutId,
        String merchantUid,
        String pgProvider,
        int amount,
        Buyer buyer
) {
    public record Buyer(String name, String email) {}
}
