package kr.farmily.api.auth.repository;

import kr.farmily.api.auth.domain.DeletionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface DeletionRequestRepository extends JpaRepository<DeletionRequest, Long> {

    Optional<DeletionRequest> findByUserId(Long userId);

    @Query("SELECT d FROM DeletionRequest d WHERE d.purgeAt < :now AND d.canceledAt IS NULL")
    List<DeletionRequest> findDueForPurge(@Param("now") OffsetDateTime now);
}
