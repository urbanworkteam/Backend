package kr.farmily.api.profile.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "sales_channels")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SalesChannelCode channel;

    @Column(nullable = false)
    private String url;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

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

    public static SalesChannel create(Long userId, SalesChannelCode code, String url, int sortOrder) {
        SalesChannel c = new SalesChannel();
        c.userId = userId;
        c.channel = code;
        c.url = url;
        c.sortOrder = sortOrder;
        return c;
    }

    public void updateUrl(String url) {
        if (url != null) this.url = url;
    }
}
