package kr.farmily.api.farmlocation.service;

import kr.farmily.api.farmlocation.client.KakaoLocalClient;
import kr.farmily.api.farmlocation.domain.FarmLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeocodingService {

    private final KakaoLocalClient kakao;
    private final KmaGridConverter grid;

    /** 주소 → lat/lng + KMA grid. 실패 시 모두 null 유지. */
    public void enrich(FarmLocation loc) {
        kakao.geocode(loc.getAddress()).ifPresentOrElse(
                latlng -> {
                    KmaGridConverter.Grid g = grid.toGrid(latlng.lat().doubleValue(), latlng.lng().doubleValue());
                    loc.applyGeocode(latlng.lat(), latlng.lng(), g.x(), g.y());
                },
                () -> log.info("Geocoding failed for: {}", loc.getAddress())
        );
    }
}
