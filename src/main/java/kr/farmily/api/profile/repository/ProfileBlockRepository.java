package kr.farmily.api.profile.repository;

import kr.farmily.api.profile.domain.BlockType;
import kr.farmily.api.profile.domain.ProfileBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfileBlockRepository extends JpaRepository<ProfileBlock, Long> {

    List<ProfileBlock> findByUserIdOrderBySortOrderAsc(Long userId);

    long countByUserIdAndBlockType(Long userId, BlockType blockType);

    void deleteByUserId(Long userId);
}
