package kr.farmily.api.subscription.scheduler;

import kr.farmily.api.subscription.domain.Subscription;
import kr.farmily.api.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreditResetScheduler {

    private final SubscriptionRepository subRepo;

    @Scheduled(cron = "0 0 0 1 * *", zone = "Asia/Seoul")
    @Transactional
    public void resetFreeMonthly() {
        List<Subscription> subs = subRepo.findAllByPlan("FREE");
        OffsetDateTime next = OffsetDateTime.now().plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        for (Subscription s : subs) s.resetUsed(next);
        log.info("Free plan credits reset: {} subscriptions", subs.size());
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void resetAllInOneDaily() {
        List<Subscription> subs = subRepo.findAllByPlan("ALL_IN_ONE");
        OffsetDateTime next = OffsetDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        for (Subscription s : subs) s.resetUsed(next);
        log.info("ALL_IN_ONE credits reset: {} subscriptions", subs.size());
    }
}
