package kr.farmily.api.subscription.service;

import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import kr.farmily.api.subscription.client.PortOneClient;
import kr.farmily.api.subscription.config.PlanProperties;
import kr.farmily.api.subscription.domain.Checkout;
import kr.farmily.api.subscription.domain.Payment;
import kr.farmily.api.subscription.domain.Subscription;
import kr.farmily.api.subscription.dto.CheckoutStartRequest;
import kr.farmily.api.subscription.dto.CheckoutStartResponse;
import kr.farmily.api.subscription.repository.CheckoutRepository;
import kr.farmily.api.subscription.repository.PaymentRepository;
import kr.farmily.api.subscription.repository.SubscriptionRepository;
import kr.farmily.api.user.domain.User;
import kr.farmily.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CheckoutRepository checkoutRepository;
    private final SubscriptionRepository subRepo;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PortOneClient portOne;
    private final PlanProperties plans;

    @Transactional
    public CheckoutStartResponse start(long userId, CheckoutStartRequest req) {
        if ("SEASON_PASS".equals(req.plan())) {
            throw new BusinessException(ErrorCode.PLAN_NOT_AVAILABLE, "준비 중인 플랜입니다", "plan");
        }
        if (!"ALL_IN_ONE".equals(req.plan())) {
            throw new BusinessException(ErrorCode.PLAN_NOT_AVAILABLE, "결제할 수 없는 플랜입니다", "plan");
        }
        int amount = plans.priceOf(req.plan());
        String merchantUid = "farmily-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
        Checkout c = checkoutRepository.save(Checkout.start(userId, req.plan(), merchantUid, amount));
        User user = userRepository.findById(userId).orElseThrow();
        return new CheckoutStartResponse(c.getId(), merchantUid, "html5_inicis", amount,
                new CheckoutStartResponse.Buyer(user.getName(), user.getEmail()));
    }

    @Transactional
    public void confirm(long userId, long checkoutId, String impUid, String merchantUid) {
        Checkout c = checkoutRepository.findById(checkoutId)
                .filter(x -> x.getUserId().equals(userId))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "결제 세션을 찾을 수 없습니다"));
        if (!c.getMerchantUid().equals(merchantUid)) {
            throw new BusinessException(ErrorCode.PAYMENT_FAILED, "merchantUid 불일치");
        }
        var pp = portOne.getPayment(impUid);
        if (!"paid".equalsIgnoreCase(pp.status())) {
            c.fail();
            throw new BusinessException(ErrorCode.PAYMENT_FAILED, "결제 상태가 paid 가 아닙니다: " + pp.status());
        }
        if (pp.amount() != c.getAmount()) {
            c.fail();
            throw new BusinessException(ErrorCode.PAYMENT_FAILED, "결제 금액 불일치");
        }
        c.confirm(impUid);

        paymentRepository.save(Payment.paid(
                userId, c.getPlan(), pp.amount(), "PORTONE", pp.impUid(), pp.merchantUid(),
                pp.paidAt() != null ? pp.paidAt() : OffsetDateTime.now(), pp.receiptUrl()
        ));

        Subscription sub = subRepo.findByUserIdForUpdate(userId)
                .orElseGet(() -> subRepo.save(Subscription.createFree(userId)));
        sub.changePlan(c.getPlan(), plans.limitOf(c.getPlan()));
        sub.updateStatus("ACTIVE");
        sub.setBillingKey(pp.customerUid());
        OffsetDateTime now = OffsetDateTime.now();
        sub.renewPeriod(now, now.plusMonths(1));
        sub.resetUsed(now.plusDays(1));
    }
}
