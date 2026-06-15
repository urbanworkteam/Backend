package kr.farmily.api.crop.service;

import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import kr.farmily.api.crop.domain.Crop;
import kr.farmily.api.crop.dto.CropPatchRequest;
import kr.farmily.api.crop.dto.CropRequest;
import kr.farmily.api.crop.repository.CropRepository;
import kr.farmily.api.common.cache.CacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CropService {

    private final CropRepository cropRepository;

    @Cacheable(cacheNames = CacheNames.CROPS, key = "#userId")
    @Transactional(readOnly = true)
    public List<Crop> listMine(long userId) {
        return cropRepository.findAllActiveByUser(userId);
    }

    @CacheEvict(cacheNames = CacheNames.CROPS, key = "#userId")
    @Transactional
    public Crop create(long userId, CropRequest req) {
        if (cropRepository.existsActiveByUserAndName(userId, req.name())) {
            throw new BusinessException(ErrorCode.HANDLE_TAKEN, "이미 등록된 작물입니다", "name");
        }
        String color = req.colorHex();
        if (color == null || color.isBlank()) {
            long idx = cropRepository.countActiveByUser(userId);
            color = CropColorPalette.pickByIndex(idx);
        }
        return cropRepository.save(Crop.create(userId, req.name(), color, req.stage()));
    }

    @CacheEvict(cacheNames = CacheNames.CROPS, key = "#userId")
    @Transactional
    public Crop update(long userId, long cropId, CropPatchRequest req) {
        Crop crop = requireOwner(userId, cropId);
        if (req.name() != null && !req.name().equals(crop.getName())
                && cropRepository.existsActiveByUserAndName(userId, req.name())) {
            throw new BusinessException(ErrorCode.HANDLE_TAKEN, "이미 등록된 작물 이름입니다", "name");
        }
        crop.update(req.name(), req.colorHex(), req.stage());
        return crop;
    }

    @CacheEvict(cacheNames = CacheNames.CROPS, key = "#userId")
    @Transactional
    public void delete(long userId, long cropId) {
        Crop crop = requireOwner(userId, cropId);
        crop.softDelete();
    }

    @Transactional(readOnly = true)
    public Crop requireOwner(long userId, long cropId) {
        return cropRepository.findActiveByIdAndUser(cropId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CROP_NOT_FOUND));
    }
}
