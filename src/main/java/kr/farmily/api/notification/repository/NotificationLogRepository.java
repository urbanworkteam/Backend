package kr.farmily.api.notification.repository;

import kr.farmily.api.notification.domain.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    boolean existsByUserIdAndKindAndSentDate(Long userId, String kind, LocalDate sentDate);
}
