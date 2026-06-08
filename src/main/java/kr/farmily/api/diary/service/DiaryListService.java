package kr.farmily.api.diary.service;

import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import kr.farmily.api.common.response.PageResponse;
import kr.farmily.api.common.upload.S3Service;
import kr.farmily.api.crop.domain.Crop;
import kr.farmily.api.crop.repository.CropRepository;
import kr.farmily.api.diary.domain.FarmDiary;
import kr.farmily.api.diary.dto.DiaryResponse;
import kr.farmily.api.diary.repository.FarmDiaryRepository;
import kr.farmily.api.farmlocation.domain.FarmLocation;
import kr.farmily.api.farmlocation.repository.FarmLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryListService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;
    private static final int DEFAULT_LOOKBACK_DAYS = 90;

    private final FarmDiaryRepository diaryRepository;
    private final CropRepository cropRepository;
    private final FarmLocationRepository farmLocationRepository;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public PageResponse<DiaryResponse> listByCrop(long userId, long cropId,
                                                  LocalDate fromDate, LocalDate toDate,
                                                  String cursor, int limit) {
        Crop crop = cropRepository.findActiveByIdAndUser(cropId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CROP_NOT_FOUND));

        LocalDate today = LocalDate.now();
        LocalDate to = toDate != null ? toDate : today;
        LocalDate from = fromDate != null ? fromDate : to.minusDays(DEFAULT_LOOKBACK_DAYS);
        if (from.isAfter(to)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "fromDate 가 toDate 보다 늦습니다");
        }
        int safeLimit = clampLimit(limit);
        Cursor parsed = Cursor.decode(cursor);

        List<FarmDiary> rows = diaryRepository.findActiveByCropPage(
                userId, cropId, from, to,
                parsed == null ? null : parsed.date(),
                parsed == null ? null : parsed.id(),
                PageRequest.of(0, safeLimit + 1));
        boolean hasMore = rows.size() > safeLimit;
        List<FarmDiary> page = hasMore ? rows.subList(0, safeLimit) : rows;

        Map<Long, FarmLocation> locations = loadLocations(page);
        List<DiaryResponse> data = page.stream()
                .map(d -> DiaryResponse.from(d,
                        d.getFarmLocationId() == null ? null : locations.get(d.getFarmLocationId()),
                        crop, s3Service::toDisplayUrl))
                .toList();

        String next = null;
        if (hasMore) {
            FarmDiary last = page.get(page.size() - 1);
            next = Cursor.encode(last.getDiaryDate(), last.getId());
        }
        return PageResponse.of(data, next, hasMore);
    }

    private int clampLimit(int limit) {
        if (limit <= 0) return DEFAULT_LIMIT;
        return Math.min(limit, MAX_LIMIT);
    }

    private Map<Long, FarmLocation> loadLocations(List<FarmDiary> diaries) {
        Set<Long> ids = new HashSet<>();
        for (FarmDiary d : diaries) {
            if (d.getFarmLocationId() != null) ids.add(d.getFarmLocationId());
        }
        if (ids.isEmpty()) return Map.of();
        return farmLocationRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(FarmLocation::getId, Function.identity()));
    }

    private record Cursor(LocalDate date, Long id) {
        static String encode(LocalDate date, Long id) {
            String raw = date.toString() + "|" + id;
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        }

        static Cursor decode(String cursor) {
            if (cursor == null || cursor.isBlank()) return null;
            try {
                String s = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
                int sep = s.indexOf('|');
                if (sep < 0) return null;
                return new Cursor(LocalDate.parse(s.substring(0, sep)), Long.parseLong(s.substring(sep + 1)));
            } catch (Exception e) {
                return null;
            }
        }
    }
}
