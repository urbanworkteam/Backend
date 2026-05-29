package kr.farmily.api.subscription.service;

import kr.farmily.api.subscription.client.PortOneClient;
import kr.farmily.api.subscription.domain.Payment;
import kr.farmily.api.subscription.domain.Subscription;
import kr.farmily.api.subscription.repository.PaymentRepository;
import kr.farmily.api.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentWebhookService {

    private final PortOneClient portOne;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subRepo;

    @Transactional
    public void process(String impUid, String merchantUid, String status) {
        if (paymentRepository.findByMerchantUid(merchantUid).isPresent()) {
            log.info("Webhook 멱등: {} 이미 처리됨", merchantUid);
            return;
        }
        var pp = portOne.getPayment(impUid);
        if (!"paid".equalsIgnoreCase(pp.status())) {
            log.warn("Webhook 결제 실패 status={}", pp.status());
            // 자동결제 실패 → GRACE 전이
            paymentRepository.findByMerchantUid(merchantUid).ifPresent(p ->
                    subRepo.findByUserIdForUpdate(p.getUserId()).ifPresent(s -> {
                        s.updateStatus("GRACE");
                        s.setGraceStartedAt(OffsetDateTime.now());
                    }));
            return;
        }
        // 정상 자동결제: subscription 가 이미 ACTIVE 라면 갱신만, 결제 레코드만 INSERT
        paymentRepository.save(Payment.paid(
                resolveUserId(pp.customerUid()), "ALL_IN_ONE", pp.amount(),
                "PORTONE", pp.impUid(), pp.merchantUid(),
                pp.paidAt() != null ? pp.paidAt() : OffsetDateTime.now(),
                pp.receiptUrl()
        ));
    }

    private Long resolveUserId(String customerUid) {
        // 단순 룩업: customer_uid 가 user_id 와 1:1 라고 가정 (운영 시 별도 매핑 테이블 권장)
        try {
            return Long.parseLong(customerUid);
        } catch (Exception e) {
            return 0L;
        }
    }
}
