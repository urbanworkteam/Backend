package kr.farmily.api.profile.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kr.farmily.api.profile.domain.SalesChannelCode;

import java.util.List;

public record UpdateProfileRequest(
        @Valid FarmEdit farm,
        String backgroundImageKey,
        String avatarImageKey,
        @Valid StoryEdit story,
        @Valid List<SalesChannelInput> salesChannels
) {

    public record FarmEdit(
            @Size(max = 50) String farmName,
            @Size(max = 50) String region,
            @Size(max = 100) String farmingMethod
    ) {}

    public record StoryEdit(
            @Size(max = 5000) String text,
            @Size(max = 5) List<String> imageKeys,
            String videoKey
    ) {}

    public record SalesChannelInput(
            SalesChannelCode channel,
            @Pattern(regexp = "^https?://.+") String url
    ) {}
}
