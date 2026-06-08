package kr.farmily.api.diary.service;

import jakarta.persistence.EntityManager;
import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import kr.farmily.api.common.upload.PhotoKeyValidator;
import kr.farmily.api.common.upload.S3Service;
import kr.farmily.api.crop.domain.Crop;
import kr.farmily.api.crop.service.CropService;
import kr.farmily.api.diary.domain.FarmDiary;
import kr.farmily.api.diary.dto.DiaryResponse;
import kr.farmily.api.diary.dto.WriteDiaryRequest;
import kr.farmily.api.diary.repository.FarmDiaryRepository;
import kr.farmily.api.farmlocation.domain.FarmLocation;
import kr.farmily.api.farmlocation.service.FarmLocationService;
import kr.farmily.api.weather.domain.WeatherSnapshot;
import kr.farmily.api.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryWriteService {

    private final FarmDiaryRepository diaryRepository;
    private final FarmLocationService farmLocationService;
    private final CropService cropService;
    private final WeatherService weatherService;
    private final PhotoKeyValidator photoKeyValidator;
    private final S3Service s3Service;
    private final EntityManager em;

    @Transactional
    public DiaryResponse write(long userId, WriteDiaryRequest req) {
        validateDate(req.date());
        FarmLocation location = farmLocationService.requireOwner(userId, req.farmLocationId());
        Crop crop = cropService.requireOwner(userId, req.cropId());
        photoKeyValidator.requireAllOwnedBy(userId, req.photoKeys());

        WeatherSnapshot weather = resolveWeather(req.weather(), location, req.date());
        FarmDiary diary = FarmDiary.create(userId, location.getId(), crop.getId(),
                req.date(), weather, req.memo());
        req.workBlocks().forEach(wb -> diary.addWorkBlock(wb.workType(), wb.detail()));
        if (req.photoKeys() != null) req.photoKeys().forEach(diary::addPhoto);

        try {
            diaryRepository.save(diary);
            em.flush();
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DIARY_ALREADY_EXISTS_FOR_DATE,
                    "해당 농장·날짜에 이미 일지가 존재합니다", "date");
        }

        return DiaryResponse.from(diary, location, crop, s3Service::toDisplayUrl);
    }

    private void validateDate(LocalDate date) {
        if (date.isAfter(LocalDate.now().plusDays(3))) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "미래 3일 이내만 작성 가능합니다", "date");
        }
    }

    private WeatherSnapshot resolveWeather(WriteDiaryRequest.WeatherInput in, FarmLocation loc, LocalDate date) {
        if ("MANUAL".equals(in.source())) {
            return WeatherSnapshot.manual(in.main(), in.tempMax(), in.tempMin(),
                    in.precipitationMm(), in.humidityPct());
        }
        try {
            return weatherService.fetchForLocation(loc, date);
        } catch (Exception e) {
            log.warn("Weather fetch failed, falling back to manual empty: {}", e.getMessage());
            return WeatherSnapshot.manual(in.main(), in.tempMax(), in.tempMin(),
                    in.precipitationMm(), in.humidityPct());
        }
    }

    @SuppressWarnings("unused")
    private static List<?> noop() { return List.of(); }
}
