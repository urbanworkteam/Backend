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
import kr.farmily.api.farmlocation.domain.FarmLocation;
import kr.farmily.api.farmlocation.service.FarmLocationService;
import kr.farmily.api.weather.domain.WeatherSnapshot;
import kr.farmily.api.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryUpdateService {

    private final DiaryReadService readService;
    private final FarmLocationService farmLocationService;
    private final CropService cropService;
    private final WeatherService weatherService;
    private final PhotoKeyValidator photoKeyValidator;
    private final S3Service s3Service;
    private final DiaryCalendarCacheEvictor calendarCacheEvictor;
    private final EntityManager em;

    @Transactional
    public DiaryResponse update(long userId, long id, WriteDiaryRequest req) {
        FarmDiary d = readService.requireOwner(userId, id);
        java.time.LocalDate previousDate = d.getDiaryDate();

        FarmLocation location = farmLocationService.requireOwner(userId, req.farmLocationId());
        Crop crop = cropService.requireOwner(userId, req.cropId());
        photoKeyValidator.requireAllOwnedBy(userId, req.photoKeys());

        d.updateFarmLocation(location.getId());
        d.updateCrop(crop.getId());
        d.updateDate(req.date());
        d.updateMemo(req.memo());

        WeatherSnapshot weather;
        if ("MANUAL".equals(req.weather().source())) {
            weather = WeatherSnapshot.manual(req.weather().main(), req.weather().tempMax(),
                    req.weather().tempMin(), req.weather().precipitationMm(), req.weather().humidityPct());
        } else {
            try {
                weather = weatherService.fetchForLocation(location, req.date());
            } catch (Exception e) {
                log.warn("Weather refetch failed: {}", e.getMessage());
                weather = WeatherSnapshot.manual(req.weather().main(), req.weather().tempMax(),
                        req.weather().tempMin(), req.weather().precipitationMm(), req.weather().humidityPct());
            }
        }
        d.updateWeather(weather);

        d.clearWorkBlocks();
        req.workBlocks().forEach(wb -> d.addWorkBlock(wb.workType(), wb.detail()));

        d.clearPhotos();
        if (req.photoKeys() != null) req.photoKeys().forEach(d::addPhoto);

        try {
            em.flush();
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DIARY_ALREADY_EXISTS_FOR_DATE,
                    "해당 농장·날짜에 이미 일지가 존재합니다", "date");
        }

        // 날짜가 바뀌었으면 이전 달·새 달 캘린더 모두 무효화
        calendarCacheEvictor.evict(userId, previousDate);
        calendarCacheEvictor.evict(userId, req.date());
        return DiaryResponse.from(d, location, crop, s3Service::toDisplayUrl);
    }
}
