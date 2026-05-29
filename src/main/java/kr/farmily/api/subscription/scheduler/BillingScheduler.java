package kr.farmily.api.subscription.scheduler;

import kr.farmily.api.subscription.client.PortOneClient;
import kr.farmily.api.subscription.config.PlanProperties;
import kr.farmily.api.subscription.domain.Payment;
import kr.farmily.api.subscription.domain.Subscription;
import kr.farmily.api.subscription.repository.PaymentRepository;
import kr.farmily.api.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingScheduler {

    private final SubscriptionRepository subRepo;
    private final PaymentRepository paymentRepository;
    private final PortOneClient portOne;
    private final PlanProperties plans;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    @Transactional
    public void chargeDueSubscriptions() {
        // ACTIVE + period_end 가 내일까지 도래하는 ALL_IN_ONE 구독 대상
        OffsetDateTime cutoff = OffsetDateTime.now().plusDays(1);
        for (Subscription s : subRepo.findAllByPlan("ALL_IN_ONE")) {
            if (!"ACTIVE".equals(s.getStatus())) continue;
            if (s.getBillingKey() == null) continue;
            if (s.getCurrentPeriodEnd().isAfter(cutoff)) continue;
            try {
                String merchantUid = "farmily-renew-" + s.getUserId() + "-" + UUID.randomUUID().toString().substring(0, 8);
                int amount = plans.priceOf(s.getPlan());
                var pp = portOne.againPay(s.getBillingKey(), merchantUid, amount, "올인원 정기결제");
                if ("paid".equalsIgnoreCase(pp.status())) {
                    paymentRepository.save(Payment.paid(s.getUserId(), s.getPlan(), pp.amount(),
                            "PORTONE", pp.impUid(), pp.merchantUid(),
                            pp.paidAt() != null ? pp.paidAt() : OffsetDateTime.now(), pp.receiptUrl()));
                    OffsetDateTime newStart = s.getCurrentPeriodEnd();
                    s.renewPeriod(newStart, newStart.plusMonths(1));
                    s.resetUsed(OffsetDateTime.now().plusDays(1));
                } else {
                    s.updateStatus("GRACE");
                    s.setGraceStartedAt(OffsetDateTime.now());
                }
            } catch (Exception e) {
                log.warn("정기결제 실패 userId={} err={}", s.getUserId(), e.getMessage());
                s.updateStatus("GRACE");
                s.setGraceStartedAt(OffsetDateTime.now());
            }
        }
    }

    @Scheduled(cron = "0 30 3 * * *", zone = "Asia/Seoul")
    @Transactional
    public void expireGrace() {
        OffsetDateTime now = OffsetDateTime.now();
        for (Subscription s : subRepo.findAllByPlan("ALL_IN_ONE")) {
            if (!"GRACE".equals(s.getStatus())) continue;
            if (s.getGraceStartedAt() == null) continue;
            if (s.getGraceStartedAt().plusDays(7).isAfter(now)) continue;
            s.changePlan("FREE", plans.limitOf("FREE"));
            s.updateStatus("EXPIRED");
            log.info("GRACE 만료 → FREE 강등: userId={}", s.getUserId());
        }
    }

    @Scheduled(cron = "0 45 3 * * *", zone = "Asia/Seoul")
    @Transactional
    public void expireCanceled() {
        OffsetDateTime now = OffsetDateTime.now();
        for (Subscription s : subRepo.findAllByPlan("ALL_IN_ONE")) {
            if (!"CANCELED".equals(s.getStatus())) continue;
            if (s.getCurrentPeriodEnd().isAfter(now)) continue;
            s.changePlan("FREE", plans.limitOf("FREE"));
            s.updateStatus("EXPIRED");
        }
    }
}
