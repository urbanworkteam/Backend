package kr.farmily.api.user.service;

import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import kr.farmily.api.crop.domain.Crop;
import kr.farmily.api.crop.repository.CropRepository;
import kr.farmily.api.farmlocation.repository.FarmLocationRepository;
import kr.farmily.api.subscription.domain.Payment;
import kr.farmily.api.subscription.domain.Subscription;
import kr.farmily.api.subscription.dto.CreditStatus;
import kr.farmily.api.subscription.repository.PaymentRepository;
import kr.farmily.api.subscription.repository.SubscriptionRepository;
import kr.farmily.api.subscription.service.CreditService;
import kr.farmily.api.user.domain.User;
import kr.farmily.api.user.dto.MyPageResponse;
import kr.farmily.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final CropRepository cropRepository;
    private final FarmLocationRepository farmLocationRepository;
    private final CreditService creditService;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public MyPageResponse getMyPage(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.isDeleted()) throw new BusinessException(ErrorCode.UNAUTHENTICATED, "탈퇴한 사용자입니다");

        List<Crop> crops = cropRepository.findAllActiveByUser(userId);
        List<String> preview = crops.stream().limit(5).map(Crop::getName).toList();
        long locCount = farmLocationRepository.countByUserId(userId);
        CreditStatus credit = creditService.getStatus(userId);

        OffsetDateTime lastPaidAt = paymentRepository.findHistoryFirstPage(userId, PageRequest.of(0, 1))
                .stream().findFirst().map(Payment::getPaidAt).orElse(null);
        OffsetDateTime nextBillingAt = subscriptionRepository.findById(userId)
                .map(Subscription::getCurrentPeriodEnd).orElse(null);

        return new MyPageResponse(
                new MyPageResponse.Account(user.getName(), null, user.getEmail()),
                new MyPageResponse.CropsSummary(crops.size(), preview),
                new MyPageResponse.FarmLocationsSummary(locCount),
                new MyPageResponse.SubscriptionSummary(
                        credit.plan(), credit.creditsUsed(), credit.creditsLimit(), credit.resetAt(),
                        lastPaidAt, nextBillingAt)
        );
    }
}
