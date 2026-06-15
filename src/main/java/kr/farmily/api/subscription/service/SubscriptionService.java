package kr.farmily.api.subscription.service;

import kr.farmily.api.common.response.PageResponse;
import kr.farmily.api.subscription.config.PlanProperties;
import kr.farmily.api.subscription.domain.Payment;
import kr.farmily.api.subscription.domain.Subscription;
import kr.farmily.api.subscription.dto.PaymentItemResponse;
import kr.farmily.api.subscription.dto.PlanItemResponse;
import kr.farmily.api.subscription.dto.SubscriptionResponse;
import kr.farmily.api.subscription.repository.PaymentRepository;
import kr.farmily.api.subscription.repository.SubscriptionRepository;
import kr.farmily.api.common.cache.CacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subRepo;
    private final PaymentRepository payRepo;
    private final PlanProperties plans;

    @Transactional
    public SubscriptionResponse getMine(long userId) {
        Subscription s = subRepo.findById(userId).orElseGet(() -> subRepo.save(Subscription.createFree(userId)));
        int limit = plans.limitOf(s.getPlan());
        boolean autoRenew = s.getBillingKey() != null && "ACTIVE".equals(s.getStatus());
        return new SubscriptionResponse(s.getPlan(), s.getStatus(), s.getCurrentPeriodEnd(),
                autoRenew, Math.max(0, limit - s.getCreditsUsed()), s.getCreditsUsed(), limit);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaymentItemResponse> getPayments(long userId, String cursor, int limit) {
        OffsetDateTime cursorAt = decode(cursor);
        List<Payment> rows = payRepo.findHistoryPage(userId, cursorAt, PageRequest.of(0, limit + 1));
        boolean hasMore = rows.size() > limit;
        List<Payment> page = hasMore ? rows.subList(0, limit) : rows;
        List<PaymentItemResponse> data = page.stream().map(p -> new PaymentItemResponse(
                p.getId(), p.getPlan(), p.getAmount(), p.getStatus(), p.getPaidAt(), p.getReceiptUrl())).toList();
        String next = hasMore ? encode(page.get(page.size() - 1).getPaidAt()) : null;
        return PageResponse.of(data, next, hasMore);
    }

    public List<PlanItemResponse> listPlans() {
        return List.of(
                new PlanItemResponse("FREE", "Free", 0, "MONTHLY",
                        plans.limitOf("FREE"), List.of("월 5회 무료", "기본 기능"), false, false, false),
                new PlanItemResponse("ALL_IN_ONE", "올인원", plans.priceOf("ALL_IN_ONE"), "MONTHLY",
                        plans.limitOf("ALL_IN_ONE"),
                        List.of("일 50회", "프리미엄 이미지 생성"), false, true, false),
                new PlanItemResponse("SEASON_PASS", "시즌 패스", 32900, "3_MONTHS",
                        null, List.of("수확 시즌 집중 사용 플랜"), true, false, true)
        );
    }

    @CacheEvict(cacheNames = CacheNames.MY_PAGE, key = "#userId")
    @Transactional
    public OffsetDateTime cancel(long userId) {
        Subscription s = subRepo.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new java.util.NoSuchElementException("구독이 없습니다"));
        s.updateStatus("CANCELED");
        return s.getCurrentPeriodEnd();
    }

    private String encode(OffsetDateTime at) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(at.toString().getBytes(StandardCharsets.UTF_8));
    }

    private OffsetDateTime decode(String cursor) {
        if (cursor == null || cursor.isBlank()) return null;
        try {
            return OffsetDateTime.parse(new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }
}
