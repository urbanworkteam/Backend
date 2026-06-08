package kr.farmily.api.diary.service;

import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import kr.farmily.api.common.upload.S3Service;
import kr.farmily.api.crop.domain.Crop;
import kr.farmily.api.crop.repository.CropRepository;
import kr.farmily.api.diary.domain.FarmDiary;
import kr.farmily.api.diary.dto.DiaryResponse;
import kr.farmily.api.diary.repository.FarmDiaryRepository;
import kr.farmily.api.farmlocation.domain.FarmLocation;
import kr.farmily.api.farmlocation.repository.FarmLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryReadService {

    private final FarmDiaryRepository diaryRepository;
    private final CropRepository cropRepository;
    private final FarmLocationRepository farmLocationRepository;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public DiaryResponse findById(long userId, long id) {
        FarmDiary d = requireOwner(userId, id);
        Crop crop = cropRepository.findById(d.getCropId()).orElse(null);
        FarmLocation loc = d.getFarmLocationId() != null
                ? farmLocationRepository.findById(d.getFarmLocationId()).orElse(null) : null;
        return DiaryResponse.from(d, loc, crop, s3Service::toDisplayUrl);
    }

    @Transactional(readOnly = true)
    public FarmDiary requireOwner(long userId, long id) {
        return diaryRepository.findActiveByIdAndUser(id, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<FarmDiary> findManyByIds(long userId, List<Long> ids) {
        return diaryRepository.findActiveByIdsAndUser(userId, ids);
    }
}
