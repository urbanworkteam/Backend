package kr.farmily.api.auth.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "auth_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthSession {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "refresh_token_hash", nullable = false)
    private String refreshTokenHash;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(columnDefinition = "inet")
    private String ip;

    @Column(name = "issued_at", nullable = false)
    private OffsetDateTime issuedAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    public static AuthSession create(Long userId, String hash, String userAgent, String ip, OffsetDateTime expiresAt) {
        AuthSession s = new AuthSession();
        s.id = UUID.randomUUID();
        s.userId = userId;
        s.refreshTokenHash = hash;
        s.userAgent = userAgent;
        s.ip = ip;
        s.issuedAt = OffsetDateTime.now();
        s.expiresAt = expiresAt;
        return s;
    }

    public void revoke() {
        this.revokedAt = OffsetDateTime.now();
    }

    public boolean isActive() {
        return revokedAt == null && expiresAt.isAfter(OffsetDateTime.now());
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(OffsetDateTime.now());
    }
}
