package kr.farmily.api.farmlocation.repository;

import kr.farmily.api.farmlocation.domain.FarmLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FarmLocationRepository extends JpaRepository<FarmLocation, Long> {

    List<FarmLocation> findByUserIdOrderBySortOrderAscIdAsc(Long userId);

    Optional<FarmLocation> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);
}
