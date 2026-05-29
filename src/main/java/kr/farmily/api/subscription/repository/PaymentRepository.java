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
            "AND (:cursorAt IS NULL OR p.paidAt < :cursorAt) " +
            "ORDER BY p.paidAt DESC, p.id DESC")
    List<Payment> findHistoryPage(@Param("userId") Long userId,
                                  @Param("cursorAt") OffsetDateTime cursorAt,
                                  Pageable pageable);
}
