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

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final KmaForecastClient client;
    private final FarmLocationService locationService;

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
        // grid+date 단위 캐시는 KmaForecastClient#fetch 의 @Cacheable 이 담당(인스턴스 간 공유).
        return client.fetch(loc.getKmaGridX(), loc.getKmaGridY(), date);
    }
}
