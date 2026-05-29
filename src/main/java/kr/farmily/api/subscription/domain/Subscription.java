package kr.farmily.api.subscription.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private String plan = "FREE";

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(name = "billing_key")
    private String billingKey;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "current_period_start", nullable = false)
    private OffsetDateTime currentPeriodStart;

    @Column(name = "current_period_end", nullable = false)
    private OffsetDateTime currentPeriodEnd;

    @Column(name = "credits_used", nullable = false)
    private int creditsUsed = 0;

    @Column(name = "credits_limit_period", nullable = false)
    private int creditsLimitPeriod = 5;

    @Column(name = "credits_reset_at", nullable = false)
    private OffsetDateTime creditsResetAt;

    @Column(name = "grace_started_at")
    private OffsetDateTime graceStartedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (startedAt == null) startedAt = now;
        if (currentPeriodStart == null) currentPeriodStart = now;
        if (currentPeriodEnd == null) currentPeriodEnd = now.plusDays(30);
        if (creditsResetAt == null) creditsResetAt = now.plusDays(30);
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public static Subscription createFree(Long userId) {
        Subscription s = new Subscription();
        s.userId = userId;
        s.plan = "FREE";
        s.status = "ACTIVE";
        s.creditsLimitPeriod = 5;
        OffsetDateTime now = OffsetDateTime.now();
        s.creditsResetAt = now.plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        return s;
    }

    public void incrementUsed() {
        creditsUsed++;
    }

    public void decrementUsed() {
        if (creditsUsed > 0) creditsUsed--;
    }

    public void resetUsed(OffsetDateTime nextReset) {
        creditsUsed = 0;
        creditsResetAt = nextReset;
    }

    public boolean isResetDue() {
        return OffsetDateTime.now().isAfter(creditsResetAt);
    }

    public void changePlan(String plan, int newLimit) {
        this.plan = plan;
        this.creditsLimitPeriod = newLimit;
    }

    public void updateStatus(String status) {
        this.status = status;
    }

    public void setBillingKey(String billingKey) {
        this.billingKey = billingKey;
    }

    public void setGraceStartedAt(OffsetDateTime t) {
        this.graceStartedAt = t;
    }

    public void renewPeriod(OffsetDateTime newStart, OffsetDateTime newEnd) {
        this.currentPeriodStart = newStart;
        this.currentPeriodEnd = newEnd;
    }
}
