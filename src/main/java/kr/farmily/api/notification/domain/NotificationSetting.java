package kr.farmily.api.notification.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "notification_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "push_enabled", nullable = false)
    private boolean pushEnabled = true;

    @Column(name = "diary_reminder_enabled", nullable = false)
    private boolean diaryReminderEnabled = true;

    @Column(name = "trend_push_enabled", nullable = false)
    private boolean trendPushEnabled = true;

    @Column(name = "marketing_push_enabled", nullable = false)
    private boolean marketingPushEnabled = false;

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

    public static NotificationSetting createDefault(Long userId) {
        NotificationSetting s = new NotificationSetting();
        s.userId = userId;
        return s;
    }

    public void update(Boolean push, Boolean diaryReminder, Boolean trend, Boolean marketing) {
        if (push != null) this.pushEnabled = push;
        if (diaryReminder != null) this.diaryReminderEnabled = diaryReminder;
        if (trend != null) this.trendPushEnabled = trend;
        if (marketing != null) this.marketingPushEnabled = marketing;
    }
}
