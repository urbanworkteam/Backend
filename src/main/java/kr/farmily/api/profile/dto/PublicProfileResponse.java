package kr.farmily.api.profile.dto;

import kr.farmily.api.profile.domain.BlockType;
import kr.farmily.api.profile.domain.SalesChannelCode;

import java.util.List;
import java.util.Map;

public record PublicProfileResponse(
        String handle,
        FarmHeader farm,
        List<SalesChannelDto> salesChannels,
        List<BlockDto> blocks
) {
    public record FarmHeader(
            String farmName, String region, String farmingMethod,
            String backgroundImageUrl, String avatarImageUrl, Story story
    ) {}

    public record Story(String text, List<String> imageUrls, String videoUrl) {}

    public record SalesChannelDto(SalesChannelCode channel, String url) {}

    public record BlockDto(BlockType blockType, int sortOrder, Map<String, Object> payload) {}
}
