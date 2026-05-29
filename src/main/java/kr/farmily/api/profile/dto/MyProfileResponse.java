package kr.farmily.api.profile.dto;

import kr.farmily.api.profile.domain.BlockType;
import kr.farmily.api.profile.domain.SalesChannelCode;

import java.util.List;
import java.util.Map;

public record MyProfileResponse(
        FarmHeader farm,
        List<SalesChannelDto> salesChannels,
        List<BlockDto> blocks
) {

    public record FarmHeader(
            String farmName,
            String region,
            String farmingMethod,
            String backgroundImageUrl,
            String avatarImageUrl,
            Story story
    ) {}

    public record Story(String text, List<String> imageUrls, String videoUrl) {}

    public record SalesChannelDto(Long id, SalesChannelCode channel, String url) {}

    public record BlockDto(Long id, BlockType blockType, int sortOrder, boolean visible, Map<String, Object> payload) {}
}
