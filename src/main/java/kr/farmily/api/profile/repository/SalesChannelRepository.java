package kr.farmily.api.profile.repository;

import kr.farmily.api.profile.domain.SalesChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesChannelRepository extends JpaRepository<SalesChannel, Long> {

    List<SalesChannel> findByUserIdOrderBySortOrderAscIdAsc(Long userId);

    void deleteByUserId(Long userId);
}
