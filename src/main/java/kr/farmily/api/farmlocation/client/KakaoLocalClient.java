package kr.farmily.api.farmlocation.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class KakaoLocalClient {

    private static final String URL = "https://dapi.kakao.com/v2/local/search/address.json";

    private final WebClient client;
    private final KakaoLocalProperties props;

    public KakaoLocalClient(KakaoLocalProperties props) {
        this.props = props;
        this.client = WebClient.builder().build();
    }

    public Optional<LatLng> geocode(String query) {
        if (props.localRestKey() == null || props.localRestKey().isBlank()) {
            log.warn("Kakao local REST key 미설정. geocode skip");
            return Optional.empty();
        }
        try {
            Map<String, Object> body = client.get()
                    .uri(uriBuilder -> uriBuilder.scheme("https").host("dapi.kakao.com")
                            .path("/v2/local/search/address.json")
                            .queryParam("query", query).build())
                    .header("Authorization", "KakaoAK " + props.localRestKey())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (body == null) return Optional.empty();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> docs = (List<Map<String, Object>>) body.get("documents");
            if (docs == null || docs.isEmpty()) return Optional.empty();
            Map<String, Object> first = docs.get(0);
            String x = (String) first.get("x");  // lng
            String y = (String) first.get("y");  // lat
            return Optional.of(new LatLng(new BigDecimal(y), new BigDecimal(x)));
        } catch (Exception e) {
            log.warn("Kakao geocode 실패 query={} err={}", query, e.getMessage());
            return Optional.empty();
        }
    }

    public record LatLng(BigDecimal lat, BigDecimal lng) {}
}
