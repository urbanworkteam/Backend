package kr.farmily.api.subscription.client;

import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import kr.farmily.api.subscription.config.PortOneProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PortOneClient {

    private static final String BASE = "https://api.iamport.kr";
    private static final String TOKEN_KEY = "portone:token";

    private final WebClient client;
    private final PortOneProperties props;
    private final StringRedisTemplate redis;
    private volatile String cachedToken;
    private volatile long cachedTokenExpiresAt;

    public PortOneClient(PortOneProperties props, StringRedisTemplate redis) {
        this.props = props;
        this.redis = redis;
        this.client = WebClient.builder().baseUrl(BASE).build();
    }

    public String issueToken() {
        long now = System.currentTimeMillis();
        // L1: 인스턴스 로컬 캐시
        if (cachedToken != null && now < cachedTokenExpiresAt - 60_000) return cachedToken;
        // L2: Redis 공유 캐시 — 인스턴스 간 토큰 재사용. Redis 장애는 무시하고 직접 발급(결제 안전).
        try {
            String shared = redis.opsForValue().get(TOKEN_KEY);
            if (shared != null && !shared.isBlank()) {
                Long ttl = redis.getExpire(TOKEN_KEY, TimeUnit.SECONDS);
                cachedToken = shared;
                cachedTokenExpiresAt = now + (ttl != null && ttl > 0 ? ttl : 60) * 1000L + 60_000;
                return cachedToken;
            }
        } catch (Exception e) {
            log.warn("PortOne token Redis 조회 실패(무시): {}", e.getMessage());
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> res = client.post()
                    .uri("/users/getToken")
                    .bodyValue(Map.of("imp_key", props.apiKey(), "imp_secret", props.apiSecret()))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (res == null) throw new BusinessException(ErrorCode.PORTONE_API_ERROR, "포트원 토큰 발급 실패");
            @SuppressWarnings("unchecked")
            Map<String, Object> response = (Map<String, Object>) res.get("response");
            if (response == null) throw new BusinessException(ErrorCode.PORTONE_API_ERROR, "포트원 토큰 응답 형식 오류");
            cachedToken = (String) response.get("access_token");
            Number exp = (Number) response.get("expired_at");
            cachedTokenExpiresAt = exp != null ? exp.longValue() * 1000L : now + 30 * 60 * 1000L;
            // Redis 저장 — 만료 60초 전까지만 공유. 장애는 무시.
            try {
                long ttlSec = (cachedTokenExpiresAt - now) / 1000L - 60;
                if (cachedToken != null && ttlSec > 0) {
                    redis.opsForValue().set(TOKEN_KEY, cachedToken, ttlSec, TimeUnit.SECONDS);
                }
            } catch (Exception e) {
                log.warn("PortOne token Redis 저장 실패(무시): {}", e.getMessage());
            }
            return cachedToken;
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.warn("PortOne token error: {}", e.getMessage());
            throw new BusinessException(ErrorCode.PORTONE_API_ERROR, "포트원 토큰 오류");
        }
    }

    public PortOnePayment getPayment(String impUid) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> res = client.get()
                    .uri("/payments/" + impUid)
                    .header("Authorization", "Bearer " + issueToken())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (res == null) throw new BusinessException(ErrorCode.PORTONE_API_ERROR, "포트원 결제 조회 실패");
            @SuppressWarnings("unchecked")
            Map<String, Object> response = (Map<String, Object>) res.get("response");
            if (response == null) throw new BusinessException(ErrorCode.PAYMENT_FAILED, "결제 정보 없음");
            return new PortOnePayment(
                    (String) response.get("imp_uid"),
                    (String) response.get("merchant_uid"),
                    (String) response.get("status"),
                    response.get("amount") instanceof Number n ? n.intValue() : 0,
                    (String) response.get("customer_uid"),
                    (String) response.get("receipt_url"),
                    OffsetDateTime.now()
            );
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.warn("PortOne getPayment error: {}", e.getMessage());
            throw new BusinessException(ErrorCode.PORTONE_API_ERROR, "포트원 결제 조회 오류");
        }
    }

    public PortOnePayment againPay(String customerUid, String merchantUid, int amount, String name) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> res = client.post()
                    .uri("/subscribe/payments/again")
                    .header("Authorization", "Bearer " + issueToken())
                    .bodyValue(Map.of(
                            "customer_uid", customerUid,
                            "merchant_uid", merchantUid,
                            "amount", amount,
                            "name", name
                    ))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (res == null) throw new BusinessException(ErrorCode.PORTONE_API_ERROR, "again 결제 실패");
            @SuppressWarnings("unchecked")
            Map<String, Object> response = (Map<String, Object>) res.get("response");
            if (response == null) throw new BusinessException(ErrorCode.PAYMENT_FAILED, "재결제 응답 누락");
            return new PortOnePayment(
                    (String) response.get("imp_uid"),
                    (String) response.get("merchant_uid"),
                    (String) response.get("status"),
                    response.get("amount") instanceof Number n ? n.intValue() : 0,
                    customerUid,
                    (String) response.get("receipt_url"),
                    OffsetDateTime.now()
            );
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            log.warn("PortOne againPay error: {}", e.getMessage());
            throw new BusinessException(ErrorCode.PORTONE_API_ERROR, "재결제 오류");
        }
    }

    public record PortOnePayment(
            String impUid, String merchantUid, String status,
            int amount, String customerUid, String receiptUrl, OffsetDateTime paidAt
    ) {}
}
