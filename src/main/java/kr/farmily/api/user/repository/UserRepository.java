package kr.farmily.api.user.repository;

import kr.farmily.api.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKakaoId(String kakaoId);

    @Query("SELECT u FROM User u WHERE u.handle = :handle AND u.deletedAt IS NULL")
    Optional<User> findActiveByHandle(@Param("handle") String handle);

    boolean existsByHandle(String handle);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NOT NULL")
    List<User> findAllSoftDeleted();

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NOT NULL AND u.deletedAt < :before")
    List<User> findSoftDeletedBefore(@Param("before") OffsetDateTime before);
}
