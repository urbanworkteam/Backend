package kr.farmily.api.auth.service;

import kr.farmily.api.auth.dto.OnboardingRequest;
import kr.farmily.api.auth.dto.OnboardingResponse;
import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import kr.farmily.api.common.util.HandleValidator;
import kr.farmily.api.crop.domain.Crop;
import kr.farmily.api.crop.dto.CropRequest;
import kr.farmily.api.crop.dto.CropResponse;
import kr.farmily.api.crop.service.CropService;
import kr.farmily.api.farmlocation.domain.FarmLocation;
import kr.farmily.api.farmlocation.dto.FarmLocationRequest;
import kr.farmily.api.farmlocation.dto.FarmLocationResponse;
import kr.farmily.api.farmlocation.service.FarmLocationService;
import kr.farmily.api.profile.domain.FarmProfile;
import kr.farmily.api.profile.repository.FarmProfileRepository;
import kr.farmily.api.user.domain.User;
import kr.farmily.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final UserRepository userRepository;
    private final FarmProfileRepository profileRepository;
    private final CropService cropService;
    private final FarmLocationService farmLocationService;
    private final HandleSuggestionService handleSuggestionService;

    @Transactional
    public OnboardingResponse complete(long userId, OnboardingRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.isOnboarded()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "이미 온보딩이 완료되었습니다");
        }
        if (!HandleValidator.isValid(req.handle())) {
            throw new BusinessException(ErrorCode.HANDLE_INVALID_FORMAT,
                    "handle 은 영문/숫자/하이픈 3-30자만 가능합니다", "handle");
        }
        if (!handleSuggestionService.isAvailable(req.handle())) {
            throw new BusinessException(ErrorCode.HANDLE_TAKEN, "이미 사용 중인 handle 입니다", "handle");
        }
        if (req.farmLocation() == null) {
            throw new BusinessException(ErrorCode.FARM_LOCATION_REQUIRED,
                    "농장 위치를 입력해주세요", "farmLocation");
        }

        Set<String> dedup = new HashSet<>();
        for (OnboardingRequest.CropEntry c : req.crops()) {
            if (!dedup.add(c.name())) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                        "중복된 작물 이름: " + c.name(), "crops");
            }
        }

        user.completeOnboarding(req.handle());

        FarmProfile profile = profileRepository.save(
                FarmProfile.create(userId, req.farmDisplayName(), req.region(), req.farmingMethod())
        );

        List<CropResponse> cropResponses = new ArrayList<>();
        for (OnboardingRequest.CropEntry c : req.crops()) {
            Crop saved = cropService.create(userId, new CropRequest(c.name(), c.colorHex(), c.stage()));
            cropResponses.add(CropResponse.from(saved));
        }

        FarmLocation loc = farmLocationService.create(userId,
                new FarmLocationRequest(
                        req.farmLocation().label(),
                        req.farmLocation().address(),
                        req.farmLocation().lat(),
                        req.farmLocation().lng()));

        return new OnboardingResponse(
                new OnboardingResponse.UserSummary(user.getId(), user.getName(), user.getHandle()),
                new OnboardingResponse.FarmProfileSummary(profile.getFarmName(), profile.getRegion(), profile.getFarmingMethod()),
                cropResponses,
                FarmLocationResponse.from(loc)
        );
    }
}
