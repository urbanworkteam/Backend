package kr.farmily.api.subscription.repository;

import kr.farmily.api.subscription.domain.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByMerchantUid(String merchantUid);

    @Query("SELECT p FROM Payment p WHERE p.userId = :userId " +
            "ORDER BY p.paidAt DESC, p.id DESC")
    List<Payment> findHistoryFirstPage(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.userId = :userId " +
            "AND p.paidAt < :cursorAt " +
            "ORDER BY p.paidAt DESC, p.id DESC")
    List<Payment> findHistoryAfter(@Param("userId") Long userId,
                                   @Param("cursorAt") OffsetDateTime cursorAt,
                                   Pageable pageable);

    default List<Payment> findHistoryPage(Long userId, OffsetDateTime cursorAt, Pageable pageable) {
        return cursorAt == null
                ? findHistoryFirstPage(userId, pageable)
                : findHistoryAfter(userId, cursorAt, pageable);
    }
}
