package kr.farmily.api.profile.service;

import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import kr.farmily.api.profile.domain.BlockType;
import kr.farmily.api.profile.domain.ProfileBlock;
import kr.farmily.api.profile.dto.BlockReorderRequest;
import kr.farmily.api.profile.dto.CreateBlockRequest;
import kr.farmily.api.profile.repository.ProfileBlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProfileBlockService {

    private final ProfileBlockRepository blockRepository;
    private final ProfileCacheEvictor cacheEvictor;

    @Transactional
    public void reorder(long userId, BlockReorderRequest req) {
        List<ProfileBlock> owned = blockRepository.findByUserIdOrderBySortOrderAsc(userId);
        if (owned.size() != req.blocks().size()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "블록 개수 불일치 (현재 " + owned.size() + ", 요청 " + req.blocks().size() + ")", "blocks");
        }
        Map<Long, ProfileBlock> byId = new HashMap<>();
        owned.forEach(b -> byId.put(b.getId(), b));

        // 임시로 음수 sort 할당 (UNIQUE 충돌 회피)
        int tmp = -1;
        for (ProfileBlock b : owned) {
            b.setSortOrder(tmp--);
        }
        blockRepository.flush();

        int finalOrder = 0;
        for (BlockReorderRequest.BlockOrder in : req.blocks()) {
            ProfileBlock target = byId.get(in.id());
            if (target == null) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "본인 소유가 아닌 블록 id: " + in.id(), "blocks");
            }
            target.applyOrder(finalOrder++, in.visible(), in.payload());
        }
        cacheEvictor.evict(userId);
    }

    @Transactional
    public ProfileBlock create(long userId, CreateBlockRequest req) {
        if (req.blockType().isSystem()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "시스템 블록은 추가할 수 없습니다", "blockType");
        }
        int nextOrder = (int) blockRepository.findByUserIdOrderBySortOrderAsc(userId).stream()
                .mapToInt(ProfileBlock::getSortOrder).max().orElse(-1) + 1;
        ProfileBlock saved = blockRepository.save(ProfileBlock.create(
                userId, req.blockType(), nextOrder, true,
                req.payload() != null ? req.payload() : new HashMap<>()));
        cacheEvictor.evict(userId);
        return saved;
    }

    @Transactional
    public void delete(long userId, long blockId) {
        ProfileBlock target = blockRepository.findById(blockId)
                .filter(b -> b.getUserId().equals(userId))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "블록을 찾을 수 없습니다"));
        if (target.getBlockType() == BlockType.TEXT) {
            blockRepository.delete(target);
            reindex(userId);
            cacheEvictor.evict(userId);
        } else {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "시스템 블록은 삭제할 수 없습니다", "blockType");
        }
    }

    private void reindex(long userId) {
        List<ProfileBlock> remaining = blockRepository.findByUserIdOrderBySortOrderAsc(userId);
        int tmp = -1;
        for (ProfileBlock b : remaining) b.setSortOrder(tmp--);
        blockRepository.flush();
        int order = 0;
        for (ProfileBlock b : remaining) b.setSortOrder(order++);
    }
}
