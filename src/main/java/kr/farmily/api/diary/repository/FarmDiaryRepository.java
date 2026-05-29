package kr.farmily.api.diary.repository;

import kr.farmily.api.diary.domain.FarmDiary;
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
}
