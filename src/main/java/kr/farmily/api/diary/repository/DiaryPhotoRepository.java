package kr.farmily.api.diary.repository;

import kr.farmily.api.diary.domain.DiaryPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiaryPhotoRepository extends JpaRepository<DiaryPhoto, Long> {

    @Query("SELECT p.s3Key FROM DiaryPhoto p WHERE p.diary.id IN :diaryIds ORDER BY p.diary.id, p.sortOrder")
    List<String> findS3KeysByDiaryIds(@Param("diaryIds") List<Long> diaryIds);
}
