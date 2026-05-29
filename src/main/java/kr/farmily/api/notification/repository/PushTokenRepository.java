package kr.farmily.api.notification.repository;

import kr.farmily.api.notification.domain.PushToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushTokenRepository extends JpaRepository<PushToken, Long> {

    Optional<PushToken> findByToken(String token);

    List<PushToken> findByUserId(Long userId);

    void deleteByToken(String token);
}
