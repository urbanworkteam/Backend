package kr.farmily.api.diary.repository;

import kr.farmily.api.diary.domain.FarmDiary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FarmDiaryRepository extends JpaRepository<FarmDiary, Long> {

    @Query("SELECT d FROM FarmDiary d WHERE d.id = :id AND d.userId = :userId AND d.deletedAt IS NULL")
    Optional<FarmDiary> findActiveByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT d FROM FarmDiary d WHERE d.userId = :userId AND d.deletedAt IS NULL " +
            "AND d.diaryDate BETWEEN :from AND :to ORDER BY d.diaryDate")
    List<FarmDiary> findActiveInRange(@Param("userId") Long userId,
                                      @Param("from") LocalDate from,
                                      @Param("to") LocalDate to);

    @Query("SELECT d FROM FarmDiary d WHERE d.userId = :userId AND d.id IN :ids AND d.deletedAt IS NULL")
    List<FarmDiary> findActiveByIdsAndUser(@Param("userId") Long userId, @Param("ids") List<Long> ids);

    @Query("SELECT d FROM FarmDiary d WHERE d.userId = :userId AND d.cropId = :cropId " +
            "AND d.deletedAt IS NULL AND d.diaryDate BETWEEN :from AND :to " +
            "ORDER BY d.diaryDate DESC, d.id DESC")
    List<FarmDiary> findActiveByCropFirstPage(@Param("userId") Long userId,
                                              @Param("cropId") Long cropId,
                                              @Param("from") LocalDate from,
                                              @Param("to") LocalDate to,
                                              Pageable pageable);

    @Query("SELECT d FROM FarmDiary d WHERE d.userId = :userId AND d.cropId = :cropId " +
            "AND d.deletedAt IS NULL AND d.diaryDate BETWEEN :from AND :to " +
            "AND (d.diaryDate < :cursorDate OR (d.diaryDate = :cursorDate AND d.id < :cursorId)) " +
            "ORDER BY d.diaryDate DESC, d.id DESC")
    List<FarmDiary> findActiveByCropAfter(@Param("userId") Long userId,
                                          @Param("cropId") Long cropId,
                                          @Param("from") LocalDate from,
                                          @Param("to") LocalDate to,
                                          @Param("cursorDate") LocalDate cursorDate,
                                          @Param("cursorId") Long cursorId,
                                          Pageable pageable);

    default List<FarmDiary> findActiveByCropPage(Long userId, Long cropId,
                                                 LocalDate from, LocalDate to,
                                                 LocalDate cursorDate, Long cursorId,
                                                 Pageable pageable) {
        if (cursorDate == null || cursorId == null) {
            return findActiveByCropFirstPage(userId, cropId, from, to, pageable);
        }
        return findActiveByCropAfter(userId, cropId, from, to, cursorDate, cursorId, pageable);
    }
}
