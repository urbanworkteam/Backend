package kr.farmily.api.subscription.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String plan;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private String pg;

    @Column(name = "pg_payment_id")
    private String pgPaymentId;

    @Column(name = "merchant_uid", unique = true)
    private String merchantUid;

    @Column(nullable = false)
    private String status;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @Column(name = "receipt_url")
    private String receiptUrl;

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

    public static Payment paid(Long userId, String plan, int amount, String pg, String pgPaymentId,
                               String merchantUid, OffsetDateTime paidAt, String receiptUrl) {
        Payment p = new Payment();
        p.userId = userId;
        p.plan = plan;
        p.amount = amount;
        p.pg = pg;
        p.pgPaymentId = pgPaymentId;
        p.merchantUid = merchantUid;
        p.status = "PAID";
        p.paidAt = paidAt;
        p.receiptUrl = receiptUrl;
        return p;
    }
}
