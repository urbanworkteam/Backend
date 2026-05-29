package kr.farmily.api.profile.service;

import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import kr.farmily.api.common.upload.PhotoKeyValidator;
import kr.farmily.api.profile.domain.FarmProfile;
import kr.farmily.api.profile.domain.SalesChannel;
import kr.farmily.api.profile.domain.SalesChannelCode;
import kr.farmily.api.profile.dto.UpdateProfileRequest;
import kr.farmily.api.profile.repository.FarmProfileRepository;
import kr.farmily.api.profile.repository.SalesChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProfileEditService {

    private final FarmProfileRepository profileRepository;
    private final SalesChannelRepository channelRepository;
    private final PhotoKeyValidator photoKeyValidator;

    @Transactional
    public void update(long userId, UpdateProfileRequest req) {
        FarmProfile p = profileRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));

        if (req.farm() != null) {
            p.updateBasic(req.farm().farmName(), req.farm().region(), req.farm().farmingMethod());
        }
        List<String> keysToVerify = new ArrayList<>();
        if (req.backgroundImageKey() != null) keysToVerify.add(req.backgroundImageKey());
        if (req.avatarImageKey() != null) keysToVerify.add(req.avatarImageKey());
        if (req.story() != null && req.story().imageKeys() != null) keysToVerify.addAll(req.story().imageKeys());
        if (req.story() != null && req.story().videoKey() != null) keysToVerify.add(req.story().videoKey());
        photoKeyValidator.requireAllOwnedBy(userId, keysToVerify);

        p.updateImages(req.backgroundImageKey(), req.avatarImageKey());
        if (req.story() != null) {
            String[] imageKeys = req.story().imageKeys() == null ? null
                    : req.story().imageKeys().toArray(String[]::new);
            p.updateStory(req.story().text(), imageKeys, req.story().videoKey());
        }

        if (req.salesChannels() != null) {
            Set<SalesChannelCode> seen = new HashSet<>();
            for (UpdateProfileRequest.SalesChannelInput input : req.salesChannels()) {
                if (!seen.add(input.channel())) {
                    throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                            "동일 채널 중복: " + input.channel(), "salesChannels");
                }
            }
            channelRepository.deleteByUserId(userId);
            channelRepository.flush();
            int order = 0;
            for (UpdateProfileRequest.SalesChannelInput input : req.salesChannels()) {
                channelRepository.save(SalesChannel.create(userId, input.channel(), input.url(), order++));
            }
        }
    }
}
