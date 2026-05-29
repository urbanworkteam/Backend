package kr.farmily.api.notification.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "push_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String platform;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "last_seen_at", nullable = false)
    private OffsetDateTime lastSeenAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (lastSeenAt == null) lastSeenAt = now;
        if (createdAt == null) createdAt = now;
    }

    public static PushToken create(Long userId, String platform, String token) {
        PushToken t = new PushToken();
        t.userId = userId;
        t.platform = platform;
        t.token = token;
        return t;
    }

    public void touch() {
        this.lastSeenAt = OffsetDateTime.now();
    }
}
