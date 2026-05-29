package kr.farmily.api.ai.repository;

import kr.farmily.api.ai.domain.ContentJob;
import kr.farmily.api.ai.domain.JobStatus;
import kr.farmily.api.ai.domain.Platform;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ContentJobRepository extends JpaRepository<ContentJob, Long> {

    Optional<ContentJob> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT j FROM ContentJob j WHERE j.userId = :userId AND j.status <> :failed " +
            "AND (:platform IS NULL OR j.platform = :platform) " +
            "AND (:cursorAt IS NULL OR j.createdAt < :cursorAt) " +
            "ORDER BY j.createdAt DESC, j.id DESC")
    List<ContentJob> findHistoryPage(@Param("userId") Long userId,
                                     @Param("platform") Platform platform,
                                     @Param("cursorAt") OffsetDateTime cursorAt,
                                     @Param("failed") JobStatus failed,
                                     Pageable pageable);

    @Query("SELECT count(j) FROM ContentJob j WHERE j.userId = :userId " +
            "AND COALESCE(j.regeneratedFrom, j.id) = :rootId AND j.id <> :rootId " +
            "AND j.createdAt > :since")
    long countRegenerationsInWindow(@Param("userId") Long userId,
                                    @Param("rootId") Long rootId,
                                    @Param("since") OffsetDateTime since);
}
