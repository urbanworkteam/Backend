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
            "ORDER BY j.createdAt DESC, j.id DESC")
    List<ContentJob> findHistoryFirstPage(@Param("userId") Long userId,
                                          @Param("failed") JobStatus failed,
                                          Pageable pageable);

    @Query("SELECT j FROM ContentJob j WHERE j.userId = :userId AND j.status <> :failed " +
            "AND j.platform = :platform " +
            "ORDER BY j.createdAt DESC, j.id DESC")
    List<ContentJob> findHistoryFirstPageByPlatform(@Param("userId") Long userId,
                                                    @Param("platform") Platform platform,
                                                    @Param("failed") JobStatus failed,
                                                    Pageable pageable);

    @Query("SELECT j FROM ContentJob j WHERE j.userId = :userId AND j.status <> :failed " +
            "AND j.createdAt < :cursorAt " +
            "ORDER BY j.createdAt DESC, j.id DESC")
    List<ContentJob> findHistoryAfter(@Param("userId") Long userId,
                                      @Param("cursorAt") OffsetDateTime cursorAt,
                                      @Param("failed") JobStatus failed,
                                      Pageable pageable);

    @Query("SELECT j FROM ContentJob j WHERE j.userId = :userId AND j.status <> :failed " +
            "AND j.platform = :platform " +
            "AND j.createdAt < :cursorAt " +
            "ORDER BY j.createdAt DESC, j.id DESC")
    List<ContentJob> findHistoryAfterByPlatform(@Param("userId") Long userId,
                                                @Param("platform") Platform platform,
                                                @Param("cursorAt") OffsetDateTime cursorAt,
                                                @Param("failed") JobStatus failed,
                                                Pageable pageable);

    default List<ContentJob> findHistoryPage(Long userId, Platform platform,
                                             OffsetDateTime cursorAt, JobStatus failed,
                                             Pageable pageable) {
        if (cursorAt == null) {
            return platform == null
                    ? findHistoryFirstPage(userId, failed, pageable)
                    : findHistoryFirstPageByPlatform(userId, platform, failed, pageable);
        }
        return platform == null
                ? findHistoryAfter(userId, cursorAt, failed, pageable)
                : findHistoryAfterByPlatform(userId, platform, cursorAt, failed, pageable);
    }

    @Query("SELECT count(j) FROM ContentJob j WHERE j.userId = :userId " +
            "AND COALESCE(j.regeneratedFrom, j.id) = :rootId AND j.id <> :rootId " +
            "AND j.createdAt > :since")
    long countRegenerationsInWindow(@Param("userId") Long userId,
                                    @Param("rootId") Long rootId,
                                    @Param("since") OffsetDateTime since);
}
