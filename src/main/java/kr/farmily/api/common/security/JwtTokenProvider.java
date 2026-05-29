package kr.farmily.api.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;

    public JwtTokenProvider(JwtProperties props) {
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTtlSeconds = props.accessTtlSeconds();
        this.refreshTtlSeconds = props.refreshTtlSeconds();
    }

    public String issueAccess(long userId, String plan, boolean onboarded) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .claim("plan", plan)
                .claim("onboarded", onboarded)
                .claim("type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlSeconds)))
                .signWith(key)
                .compact();
    }

    public String issueRefresh(long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTtlSeconds)))
                .signWith(key)
                .compact();
    }

    public CurrentUser parseAccess(String token) {
        Claims claims = parse(token);
        if (!"access".equals(claims.get("type"))) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "access token required");
        }
        long id = Long.parseLong(claims.getSubject());
        String plan = claims.get("plan", String.class);
        Boolean onboarded = claims.get("onboarded", Boolean.class);
        return CurrentUser.of(id, plan != null ? plan : "FREE", Boolean.TRUE.equals(onboarded));
    }

    public long parseRefreshUserId(String token) {
        Claims claims = parse(token);
        if (!"refresh".equals(claims.get("type"))) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "refresh token required");
        }
        return Long.parseLong(claims.getSubject());
    }

    public long accessTtlSeconds() {
        return accessTtlSeconds;
    }

    public long refreshTtlSeconds() {
        return refreshTtlSeconds;
    }

    private Claims parse(String token) {
        try {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "토큰이 유효하지 않습니다");
        }
    }
}
