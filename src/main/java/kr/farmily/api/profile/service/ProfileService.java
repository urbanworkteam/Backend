package kr.farmily.api.profile.service;

import kr.farmily.api.common.config.S3Properties;
import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import kr.farmily.api.profile.domain.BlockType;
import kr.farmily.api.profile.domain.FarmProfile;
import kr.farmily.api.profile.domain.ProfileBlock;
import kr.farmily.api.profile.domain.SalesChannel;
import kr.farmily.api.profile.dto.MyProfileResponse;
import kr.farmily.api.profile.repository.FarmProfileRepository;
import kr.farmily.api.profile.repository.ProfileBlockRepository;
import kr.farmily.api.profile.repository.SalesChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final FarmProfileRepository profileRepository;
    private final ProfileBlockRepository blockRepository;
    private final SalesChannelRepository channelRepository;
    private final S3Properties s3Properties;

    @Transactional
    public MyProfileResponse getMyProfile(long userId) {
        FarmProfile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));

        List<ProfileBlock> blocks = blockRepository.findByUserIdOrderBySortOrderAsc(userId);
        if (blocks.isEmpty()) {
            blocks = seedDefaultBlocks(userId);
        }

        List<SalesChannel> channels = channelRepository.findByUserIdOrderBySortOrderAscIdAsc(userId);

        return new MyProfileResponse(
                new MyProfileResponse.FarmHeader(
                        profile.getFarmName(),
                        profile.getRegion(),
                        profile.getFarmingMethod(),
                        toCdnUrl(profile.getBackgroundImageKey()),
                        toCdnUrl(profile.getAvatarImageKey()),
                        new MyProfileResponse.Story(
                                profile.getStoryText(),
                                profile.getStoryImageKeys() == null ? List.of()
                                        : List.of(profile.getStoryImageKeys()).stream().map(this::toCdnUrl).toList(),
                                toCdnUrl(profile.getStoryVideoKey())
                        )
                ),
                channels.stream().map(c -> new MyProfileResponse.SalesChannelDto(c.getId(), c.getChannel(), c.getUrl())).toList(),
                blocks.stream().map(b -> new MyProfileResponse.BlockDto(
                        b.getId(), b.getBlockType(), b.getSortOrder(), b.getVisible(), b.getPayload()
                )).toList()
        );
    }

    @Transactional
    public List<ProfileBlock> seedDefaultBlocks(long userId) {
        List<ProfileBlock> seeds = List.of(
                ProfileBlock.create(userId, BlockType.CROP_INTRO, 0, true, new HashMap<>()),
                ProfileBlock.create(userId, BlockType.STORY, 1, true, new HashMap<>()),
                ProfileBlock.create(userId, BlockType.CALENDAR, 2, true, new HashMap<>()),
                ProfileBlock.create(userId, BlockType.DIVIDER, 3, true, new HashMap<>()),
                ProfileBlock.create(userId, BlockType.TEXT, 4, false, new HashMap<>(java.util.Map.of("body", "")))
        );
        return blockRepository.saveAll(seeds);
    }

    public String toCdnUrl(String key) {
        if (key == null || key.isBlank()) return null;
        if (s3Properties.cdnBaseUrl() == null || s3Properties.cdnBaseUrl().isBlank()) return key;
        return s3Properties.cdnBaseUrl().replaceAll("/$", "") + "/" + key;
    }
}
