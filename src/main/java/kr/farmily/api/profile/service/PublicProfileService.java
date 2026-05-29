package kr.farmily.api.profile.service;

import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import kr.farmily.api.profile.domain.FarmProfile;
import kr.farmily.api.profile.domain.ProfileBlock;
import kr.farmily.api.profile.domain.SalesChannel;
import kr.farmily.api.profile.dto.PublicProfileResponse;
import kr.farmily.api.profile.repository.FarmProfileRepository;
import kr.farmily.api.profile.repository.ProfileBlockRepository;
import kr.farmily.api.profile.repository.SalesChannelRepository;
import kr.farmily.api.user.domain.User;
import kr.farmily.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicProfileService {

    private final UserRepository userRepository;
    private final FarmProfileRepository profileRepository;
    private final ProfileBlockRepository blockRepository;
    private final SalesChannelRepository channelRepository;
    private final ProfileService profileService;

    @Transactional(readOnly = true)
    public PublicProfileResponse findByHandle(String handle) {
        User user = userRepository.findActiveByHandle(handle)
                .filter(User::isOnboarded)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "농장을 찾을 수 없습니다"));

        FarmProfile profile = profileRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "농장을 찾을 수 없습니다"));

        List<ProfileBlock> blocks = blockRepository.findByUserIdOrderBySortOrderAsc(user.getId()).stream()
                .filter(ProfileBlock::getVisible)
                .toList();
        List<SalesChannel> channels = channelRepository.findByUserIdOrderBySortOrderAscIdAsc(user.getId());

        return new PublicProfileResponse(
                user.getHandle(),
                new PublicProfileResponse.FarmHeader(
                        profile.getFarmName(), profile.getRegion(), profile.getFarmingMethod(),
                        profileService.toCdnUrl(profile.getBackgroundImageKey()),
                        profileService.toCdnUrl(profile.getAvatarImageKey()),
                        new PublicProfileResponse.Story(
                                profile.getStoryText(),
                                profile.getStoryImageKeys() == null ? List.of()
                                        : List.of(profile.getStoryImageKeys()).stream().map(profileService::toCdnUrl).toList(),
                                profileService.toCdnUrl(profile.getStoryVideoKey())
                        )
                ),
                channels.stream().map(c -> new PublicProfileResponse.SalesChannelDto(c.getChannel(), c.getUrl())).toList(),
                blocks.stream().map(b -> new PublicProfileResponse.BlockDto(
                        b.getBlockType(), b.getSortOrder(), b.getPayload()
                )).toList()
        );
    }

    @Transactional(readOnly = true)
    public boolean handleExists(String handle) {
        return userRepository.findActiveByHandle(handle).filter(User::isOnboarded).isPresent();
    }

    @Transactional(readOnly = true)
    public Long resolveUserId(String handle) {
        return userRepository.findActiveByHandle(handle)
                .filter(User::isOnboarded)
                .map(User::getId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "농장을 찾을 수 없습니다"));
    }
}
