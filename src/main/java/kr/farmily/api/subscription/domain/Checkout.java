package kr.farmily.api.subscription.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "checkouts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Checkout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String plan;

    @Column(name = "merchant_uid", nullable = false, unique = true)
    private String merchantUid;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "pg_payment_id")
    private String pgPaymentId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "confirmed_at")
    private OffsetDateTime confirmedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
    }

    public static Checkout start(Long userId, String plan, String merchantUid, int amount) {
        Checkout c = new Checkout();
        c.userId = userId;
        c.plan = plan;
        c.merchantUid = merchantUid;
        c.amount = amount;
        return c;
    }

    public void confirm(String pgPaymentId) {
        this.status = "CONFIRMED";
        this.pgPaymentId = pgPaymentId;
        this.confirmedAt = OffsetDateTime.now();
    }

    public void fail() {
        this.status = "FAILED";
        this.confirmedAt = OffsetDateTime.now();
    }
}
