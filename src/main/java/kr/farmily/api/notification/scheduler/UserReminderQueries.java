package kr.farmily.api.notification.scheduler;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserReminderQueries {

    private final EntityManager em;

    @Transactional(readOnly = true)
    public List<Long> findUsersWithoutDiaryOn(LocalDate date) {
        String sql = """
                SELECT u.id FROM users u
                  LEFT JOIN notification_settings n ON n.user_id = u.id
                 WHERE u.deleted_at IS NULL
                   AND u.onboarded_at IS NOT NULL
                   AND coalesce(n.diary_reminder_enabled, true) = true
                   AND coalesce(n.push_enabled, true) = true
                   AND NOT EXISTS (
                     SELECT 1 FROM farm_diaries d
                      WHERE d.user_id = u.id AND d.diary_date = :today AND d.deleted_at IS NULL
                   )
                """;
        @SuppressWarnings("unchecked")
        List<Number> rows = em.createNativeQuery(sql)
                .setParameter("today", date)
                .getResultList();
        return rows.stream().map(Number::longValue).toList();
    }
}
