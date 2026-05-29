package kr.farmily.api.auth.dto;

public record AuthTokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        boolean isNewUser,
        UserSummary user
) {

    public record UserSummary(Long id, String name, String handle, boolean onboarded) {}
}
