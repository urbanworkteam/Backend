package kr.farmily.api.crop.repository;

import kr.farmily.api.crop.domain.Crop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CropRepository extends JpaRepository<Crop, Long> {

    @Query("SELECT c FROM Crop c WHERE c.userId = :userId AND c.deletedAt IS NULL ORDER BY c.id")
    List<Crop> findAllActiveByUser(@Param("userId") Long userId);

    @Query("SELECT c FROM Crop c WHERE c.id = :id AND c.userId = :userId AND c.deletedAt IS NULL")
    Optional<Crop> findActiveByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT count(c) > 0 FROM Crop c WHERE c.userId = :userId AND c.name = :name AND c.deletedAt IS NULL")
    boolean existsActiveByUserAndName(@Param("userId") Long userId, @Param("name") String name);

    @Query("SELECT count(c) FROM Crop c WHERE c.userId = :userId AND c.deletedAt IS NULL")
    long countActiveByUser(@Param("userId") Long userId);
}
