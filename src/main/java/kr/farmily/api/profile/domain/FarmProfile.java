package kr.farmily.api.profile.domain;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;

@Entity
@Table(name = "farm_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FarmProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "farm_name")
    private String farmName;

    private String region;

    @Column(name = "farming_method")
    private String farmingMethod;

    @Column(name = "background_image_key")
    private String backgroundImageKey;

    @Column(name = "avatar_image_key")
    private String avatarImageKey;

    @Column(name = "story_text")
    private String storyText;

    @Type(StringArrayType.class)
    @Column(name = "story_image_keys", columnDefinition = "text[]")
    private String[] storyImageKeys;

    @Column(name = "story_video_key")
    private String storyVideoKey;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public static FarmProfile create(Long userId, String farmName, String region, String farmingMethod) {
        FarmProfile p = new FarmProfile();
        p.userId = userId;
        p.farmName = farmName;
        p.region = region;
        p.farmingMethod = farmingMethod;
        return p;
    }

    public void updateBasic(String farmName, String region, String farmingMethod) {
        if (farmName != null) this.farmName = farmName;
        if (region != null) this.region = region;
        if (farmingMethod != null) this.farmingMethod = farmingMethod;
    }

    public void updateImages(String backgroundKey, String avatarKey) {
        if (backgroundKey != null) this.backgroundImageKey = backgroundKey;
        if (avatarKey != null) this.avatarImageKey = avatarKey;
    }

    public void updateStory(String text, String[] imageKeys, String videoKey) {
        if (text != null) this.storyText = text;
        if (imageKeys != null) this.storyImageKeys = imageKeys;
        if (videoKey != null) this.storyVideoKey = videoKey;
    }
}
