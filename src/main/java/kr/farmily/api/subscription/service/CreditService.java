package kr.farmily.api.subscription.service;

import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import kr.farmily.api.subscription.config.PlanProperties;
import kr.farmily.api.subscription.domain.Subscription;
import kr.farmily.api.subscription.dto.CreditStatus;
import kr.farmily.api.subscription.repository.SubscriptionRepository;
import kr.farmily.api.common.cache.CacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class CreditService {

    private final SubscriptionRepository subRepo;
    private final CreditIdempotencyStore idem;
    private final PlanProperties plans;

    @CacheEvict(cacheNames = CacheNames.MY_PAGE, key = "#userId")
    @Transactional
    public void tryConsume(long userId, String idemKey) {
        if (idemKey == null || idemKey.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "idempotency key 필요", "idemKey");
        }
        if (!idem.tryAcquire(userId, idemKey)) return;
        Subscription sub = subRepo.findByUserIdForUpdate(userId)
                .orElseGet(() -> subRepo.save(Subscription.createFree(userId)));
        applyResetIfDue(sub);
        int limit = plans.limitOf(sub.getPlan());
        if (sub.getCreditsUsed() >= limit) {
            idem.release(userId, idemKey);
            throw new BusinessException(ErrorCode.CREDIT_EXHAUSTED,
                    "크레딧 한도(" + limit + ")를 초과했습니다");
        }
        sub.incrementUsed();
    }

    @CacheEvict(cacheNames = CacheNames.MY_PAGE, key = "#userId")
    @Transactional
    public void refund(long userId, String idemKey) {
        subRepo.findByUserIdForUpdate(userId).ifPresent(Subscription::decrementUsed);
        if (idemKey != null) idem.release(userId, idemKey);
    }

    @Transactional(readOnly = true)
    public CreditStatus getStatus(long userId) {
        Subscription sub = subRepo.findById(userId)
                .orElseGet(() -> subRepo.save(Subscription.createFree(userId)));
        int limit = plans.limitOf(sub.getPlan());
        return new CreditStatus(sub.getPlan(),
                Math.max(0, limit - sub.getCreditsUsed()),
                limit,
                sub.getCreditsUsed(),
                sub.getCreditsResetAt());
    }

    private void applyResetIfDue(Subscription sub) {
        if (!sub.isResetDue()) return;
        OffsetDateTime nextReset = "ALL_IN_ONE".equals(sub.getPlan())
                ? OffsetDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
                : OffsetDateTime.now().plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        sub.resetUsed(nextReset);
    }
}
