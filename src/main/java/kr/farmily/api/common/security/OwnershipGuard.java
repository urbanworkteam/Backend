package kr.farmily.api.common.security;

import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;

public final class OwnershipGuard {

    private OwnershipGuard() {}

    public static void require(CurrentUser user, long ownerId) {
        if (user.id() != ownerId) {
            throw new BusinessException(ErrorCode.NOT_RESOURCE_OWNER, "본인 자원이 아닙니다");
        }
    }

    public static void requireOnboarded(CurrentUser user) {
        if (!user.onboarded()) {
            throw new BusinessException(ErrorCode.ONBOARDING_REQUIRED, "온보딩을 먼저 완료해주세요");
        }
    }
}
