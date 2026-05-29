package kr.farmily.api.subscription.repository;

import jakarta.persistence.LockModeType;
import kr.farmily.api.subscription.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Subscription s WHERE s.userId = :userId")
    Optional<Subscription> findByUserIdForUpdate(@Param("userId") Long userId);

    @Query("SELECT s FROM Subscription s WHERE s.plan = :plan")
    List<Subscription> findAllByPlan(@Param("plan") String plan);
}
