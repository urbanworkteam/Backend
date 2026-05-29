package kr.farmily.api.auth.repository;

import kr.farmily.api.auth.domain.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {

    Optional<AuthSession> findByRefreshTokenHash(String hash);

    @Modifying
    @Query("UPDATE AuthSession s SET s.revokedAt = :now WHERE s.userId = :userId AND s.revokedAt IS NULL")
    void revokeAllForUser(@Param("userId") Long userId, @Param("now") OffsetDateTime now);
}
