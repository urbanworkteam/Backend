package kr.farmily.api.ai.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "content_jobs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @Column(name = "crop_id", nullable = false)
    private Long cropId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "diary_ids", columnDefinition = "bigint[]")
    private Long[] diaryIds;

    private String keywords;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "extra_photo_keys", columnDefinition = "text[]")
    private String[] extraPhotoKeys;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.QUEUED;

    @Column(name = "progress_pct", nullable = false)
    private Short progressPct = 0;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "regenerated_from")
    private Long regeneratedFrom;

    @Column(name = "credit_charged", nullable = false)
    private Boolean creditCharged = false;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "done_at")
    private OffsetDateTime doneAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public static ContentJob create(Long userId, Platform platform, Long cropId,
                                    Long[] diaryIds, String keywords, String[] extraPhotoKeys,
                                    boolean creditCharged, Long regeneratedFrom) {
        ContentJob j = new ContentJob();
        j.userId = userId;
        j.platform = platform;
        j.cropId = cropId;
        j.diaryIds = diaryIds;
        j.keywords = keywords;
        j.extraPhotoKeys = extraPhotoKeys;
        j.creditCharged = creditCharged;
        j.regeneratedFrom = regeneratedFrom;
        return j;
    }

    public void transition(JobStatus next, int progress) {
        this.status = next;
        this.progressPct = (short) progress;
        if (next.isTerminal()) this.doneAt = OffsetDateTime.now();
    }

    public void fail(String reason) {
        this.status = JobStatus.FAILED;
        this.failureReason = reason;
        this.doneAt = OffsetDateTime.now();
    }

    public void markRefunded() {
        this.status = JobStatus.REFUNDED;
    }
}
