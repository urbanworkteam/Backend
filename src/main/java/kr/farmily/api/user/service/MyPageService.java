package kr.farmily.api.user.service;

import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import kr.farmily.api.crop.domain.Crop;
import kr.farmily.api.crop.repository.CropRepository;
import kr.farmily.api.farmlocation.repository.FarmLocationRepository;
import kr.farmily.api.subscription.dto.CreditStatus;
import kr.farmily.api.subscription.service.CreditService;
import kr.farmily.api.user.domain.User;
import kr.farmily.api.user.dto.MyPageResponse;
import kr.farmily.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final CropRepository cropRepository;
    private final FarmLocationRepository farmLocationRepository;
    private final CreditService creditService;

    @Transactional(readOnly = true)
    public MyPageResponse getMyPage(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.isDeleted()) throw new BusinessException(ErrorCode.UNAUTHENTICATED, "탈퇴한 사용자입니다");

        List<Crop> crops = cropRepository.findAllActiveByUser(userId);
        List<String> preview = crops.stream().limit(5).map(Crop::getName).toList();
        long locCount = farmLocationRepository.countByUserId(userId);
        CreditStatus credit = creditService.getStatus(userId);

        return new MyPageResponse(
                new MyPageResponse.Account(user.getName(), null, user.getEmail()),
                new MyPageResponse.CropsSummary(crops.size(), preview),
                new MyPageResponse.FarmLocationsSummary(locCount),
                new MyPageResponse.SubscriptionSummary(
                        credit.plan(), credit.creditsUsed(), credit.creditsLimit(), credit.resetAt())
        );
    }
}
