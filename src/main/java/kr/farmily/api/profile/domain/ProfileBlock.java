package kr.farmily.api.profile.domain;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "profile_blocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "block_type", nullable = false)
    private BlockType blockType;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private Boolean visible = true;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> payload = new HashMap<>();

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

    public static ProfileBlock create(Long userId, BlockType type, int sortOrder, boolean visible, Map<String, Object> payload) {
        ProfileBlock b = new ProfileBlock();
        b.userId = userId;
        b.blockType = type;
        b.sortOrder = sortOrder;
        b.visible = visible;
        b.payload = payload != null ? payload : new HashMap<>();
        return b;
    }

    public void applyOrder(int sortOrder, Boolean visible, Map<String, Object> payload) {
        this.sortOrder = sortOrder;
        if (visible != null) this.visible = visible;
        if (payload != null) this.payload = payload;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
