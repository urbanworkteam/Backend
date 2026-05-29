package kr.farmily.api.weather.service;

import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import kr.farmily.api.farmlocation.domain.FarmLocation;
import kr.farmily.api.farmlocation.service.FarmLocationService;
import kr.farmily.api.weather.client.KmaForecastClient;
import kr.farmily.api.weather.domain.WeatherSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private static final long TTL_MILLIS = 60 * 60 * 1000L; // 1시간

    private final KmaForecastClient client;
    private final FarmLocationService locationService;
    private final Map<String, Cached> cache = new ConcurrentHashMap<>();

    public WeatherSnapshot fetchForUser(long userId, long farmLocationId, LocalDate date) {
        FarmLocation loc = locationService.requireOwner(userId, farmLocationId);
        return fetchForLocation(loc, date);
    }

    public WeatherSnapshot fetchForLocation(FarmLocation loc, LocalDate date) {
        if (!loc.hasGrid()) {
            return WeatherSnapshot.empty();
        }
        LocalDate today = LocalDate.now();
        if (date.isAfter(today.plusDays(3))) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "단기예보 범위 초과 (최대 +3일)", "date");
        }
        String key = loc.getKmaGridX() + ":" + loc.getKmaGridY() + ":" + date;
        Cached hit = cache.get(key);
        long now = System.currentTimeMillis();
        if (hit != null && now - hit.at < TTL_MILLIS) {
            return hit.snapshot;
        }
        WeatherSnapshot fresh = client.fetch(loc.getKmaGridX(), loc.getKmaGridY(), date);
        cache.put(key, new Cached(fresh, now));
        return fresh;
    }

    private record Cached(WeatherSnapshot snapshot, long at) {}

    @SuppressWarnings("unused")
    private static LocalDateTime nowKst() {
        return LocalDateTime.now();
    }
}
