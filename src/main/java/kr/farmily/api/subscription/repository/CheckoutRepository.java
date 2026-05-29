package kr.farmily.api.subscription.repository;

import kr.farmily.api.subscription.domain.Checkout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CheckoutRepository extends JpaRepository<Checkout, Long> {

    Optional<Checkout> findByMerchantUid(String merchantUid);
}
