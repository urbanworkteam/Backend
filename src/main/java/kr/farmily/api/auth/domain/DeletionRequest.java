package kr.farmily.api.auth.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "deletion_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeletionRequest {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "requested_at", nullable = false)
    private OffsetDateTime requestedAt;

    @Column(name = "purge_at", nullable = false)
    private OffsetDateTime purgeAt;

    @Column(name = "canceled_at")
    private OffsetDateTime canceledAt;

    public static DeletionRequest open(Long userId, int graceDays) {
        DeletionRequest r = new DeletionRequest();
        OffsetDateTime now = OffsetDateTime.now();
        r.userId = userId;
        r.requestedAt = now;
        r.purgeAt = now.plusDays(graceDays);
        return r;
    }

    public void cancel() {
        this.canceledAt = OffsetDateTime.now();
    }

    public boolean isCanceled() {
        return canceledAt != null;
    }
}
