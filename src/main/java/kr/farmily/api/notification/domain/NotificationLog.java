package kr.farmily.api.notification.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "notification_logs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "kind", "sent_date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String kind;

    @Column(name = "sent_date", nullable = false)
    private LocalDate sentDate;

    public static NotificationLog of(Long userId, String kind, LocalDate date) {
        NotificationLog l = new NotificationLog();
        l.userId = userId;
        l.kind = kind;
        l.sentDate = date;
        return l;
    }
}
