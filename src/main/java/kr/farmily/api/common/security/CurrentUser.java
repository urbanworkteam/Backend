package kr.farmily.api.common.security;

public record CurrentUser(long id, String plan, boolean onboarded) {

    public static CurrentUser of(long id, String plan, boolean onboarded) {
        return new CurrentUser(id, plan, onboarded);
    }
}
