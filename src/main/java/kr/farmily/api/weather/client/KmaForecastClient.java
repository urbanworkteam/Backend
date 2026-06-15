package kr.farmily.api.weather.client;

import kr.farmily.api.common.cache.CacheNames;
import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import kr.farmily.api.weather.domain.WeatherSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class KmaForecastClient {

    private static final int[] BASE_TIMES = {2, 5, 8, 11, 14, 17, 20, 23};
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final WebClient client;
    private final KmaProperties props;

    public KmaForecastClient(KmaProperties props) {
        this.props = props;
        this.client = WebClient.builder().build();
    }

    // grid+date 단위로 Redis 캐시(60분). 별도 빈이라 WeatherService 의 두 진입점 모두 프록시 경유 → 캐시 적용.
    // 키 미설정/빈 스냅샷(source=MANUAL)은 캐시하지 않음.
    @Cacheable(cacheNames = CacheNames.WEATHER, key = "#gridX + ':' + #gridY + ':' + #date",
            unless = "#result == null || #result.source == 'MANUAL'")
    public WeatherSnapshot fetch(int gridX, int gridY, LocalDate date) {
        if (props.serviceKey() == null || props.serviceKey().isBlank()) {
            log.warn("KMA service key 미설정. 빈 스냅샷 반환");
            return WeatherSnapshot.empty();
        }
        ZonedDateTime now = ZonedDateTime.now(KST);
        BaseTime baseTime = pickRecentBase(now);

        try {
            Map<String, Object> body = client.get()
                    .uri(uriBuilder -> uriBuilder.scheme("https").host(host()).path(path())
                            .queryParam("serviceKey", props.serviceKey())
                            .queryParam("numOfRows", 1000)
                            .queryParam("pageNo", 1)
                            .queryParam("dataType", "JSON")
                            .queryParam("base_date", baseTime.date.format(DateTimeFormatter.BASIC_ISO_DATE))
                            .queryParam("base_time", String.format("%02d00", baseTime.hour))
                            .queryParam("nx", gridX)
                            .queryParam("ny", gridY)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return parse(body, date);
        } catch (Exception e) {
            log.warn("KMA 호출 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.KMA_API_ERROR, "기상청 단기예보 조회 실패");
        }
    }

    @SuppressWarnings("unchecked")
    private WeatherSnapshot parse(Map<String, Object> body, LocalDate targetDate) {
        if (body == null) return WeatherSnapshot.empty();
        Map<String, Object> response = (Map<String, Object>) body.get("response");
        if (response == null) return WeatherSnapshot.empty();
        Map<String, Object> bodyMap = (Map<String, Object>) response.get("body");
        if (bodyMap == null) return WeatherSnapshot.empty();
        Map<String, Object> items = (Map<String, Object>) bodyMap.get("items");
        if (items == null) return WeatherSnapshot.empty();
        List<Map<String, Object>> list = (List<Map<String, Object>>) items.get("item");
        if (list == null || list.isEmpty()) return WeatherSnapshot.empty();

        String target = targetDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        Map<String, String> byCategory = new HashMap<>();
        BigDecimal tempMax = null, tempMin = null;
        BigDecimal tmpMax = null, tmpMin = null;
        BigDecimal precipitation = BigDecimal.ZERO;
        Integer humidity = null;
        String sky = null;
        String pty = null;

        for (Map<String, Object> item : list) {
            if (!target.equals(String.valueOf(item.get("fcstDate")))) continue;
            String cat = (String) item.get("category");
            String v = String.valueOf(item.get("fcstValue"));
            byCategory.put(cat, v);
            switch (cat) {
                case "TMX" -> tempMax = parseDecimal(v);
                case "TMN" -> tempMin = parseDecimal(v);
                case "TMP" -> {
                    BigDecimal t = parseDecimal(v);
                    if (t != null) {
                        if (tmpMax == null || t.compareTo(tmpMax) > 0) tmpMax = t;
                        if (tmpMin == null || t.compareTo(tmpMin) < 0) tmpMin = t;
                    }
                }
                case "REH" -> humidity = parseInt(v);
                case "SKY" -> { if (sky == null) sky = v; }
                case "PTY" -> { if (pty == null) pty = v; }
                case "PCP" -> {
                    BigDecimal p = parsePcp(v);
                    if (p != null && p.compareTo(precipitation) > 0) precipitation = p;
                }
                default -> {}
            }
        }
        // TMX/TMN 이 발표되지 않은 base_time 의 경우 (오후/저녁 호출) TMP 시간별 값에서 폴백
        if (tempMax == null) tempMax = tmpMax;
        if (tempMin == null) tempMin = tmpMin;
        String main = mainOf(sky, pty);
        return WeatherSnapshot.kma(main, tempMax, tempMin, precipitation, humidity);
    }

    private String host() {
        try {
            return new java.net.URI(props.baseUrl()).getHost();
        } catch (Exception e) {
            return "apis.data.go.kr";
        }
    }

    private String path() {
        try {
            return new java.net.URI(props.baseUrl()).getPath() + "/getVilageFcst";
        } catch (Exception e) {
            return "/1360000/VilageFcstInfoService_2.0/getVilageFcst";
        }
    }

    private record BaseTime(LocalDate date, int hour) {}

    private BaseTime pickRecentBase(ZonedDateTime now) {
        int hour = now.getHour();
        // 발표 시점은 0210, 0510, ..., 2310. 안전마진 10분 → +10분 이후 사용.
        int pick = -1;
        for (int b : BASE_TIMES) {
            if (hour > b || (hour == b && now.getMinute() >= 15)) pick = b;
        }
        if (pick == -1) {
            return new BaseTime(now.toLocalDate().minusDays(1), 23);
        }
        return new BaseTime(now.toLocalDate(), pick);
    }

    private static BigDecimal parseDecimal(String v) {
        try { return new BigDecimal(v); } catch (Exception e) { return null; }
    }

    private static Integer parseInt(String v) {
        try { return Integer.parseInt(v); } catch (Exception e) { return null; }
    }

    private static BigDecimal parsePcp(String v) {
        if (v == null || v.isBlank() || "강수없음".equals(v)) return BigDecimal.ZERO;
        String n = v.replaceAll("[^0-9.]", "");
        return n.isBlank() ? BigDecimal.ZERO : new BigDecimal(n);
    }

    private static String mainOf(String sky, String pty) {
        if ("1".equals(pty)) return "비";
        if ("2".equals(pty)) return "비/눈";
        if ("3".equals(pty)) return "눈";
        if ("4".equals(pty)) return "소나기";
        return switch (sky) {
            case "1" -> "맑음";
            case "3" -> "구름많음";
            case "4" -> "흐림";
            case null -> "맑음";
            default -> "맑음";
        };
    }

    static {
        // suppress -Wno-warning-unused
        LocalTime.MIDNIGHT.toString();
    }
}
