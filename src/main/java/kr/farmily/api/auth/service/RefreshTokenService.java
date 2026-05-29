package kr.farmily.api.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import kr.farmily.api.auth.domain.AuthSession;
import kr.farmily.api.auth.dto.AuthTokenResponse;
import kr.farmily.api.auth.repository.AuthSessionRepository;
import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import kr.farmily.api.common.security.JwtTokenProvider;
import kr.farmily.api.user.domain.User;
import kr.farmily.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final AuthSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwt;

    @Transactional
    public AuthTokenResponse rotate(String refreshToken, HttpServletRequest httpReq) {
        String hash = TokenHasher.sha256(refreshToken);
        AuthSession session = sessionRepository.findByRefreshTokenHash(hash)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN, "refresh 토큰을 찾을 수 없습니다"));

        if (session.isRevoked()) {
            // 보안 강화: 재사용 시도 시 동일 사용자 전체 무효화
            sessionRepository.revokeAllForUser(session.getUserId(), OffsetDateTime.now());
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "이미 무효화된 토큰입니다");
        }
        if (session.isExpired()) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "만료된 토큰입니다");
        }

        session.revoke();

        User user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.isDeleted()) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED, "탈퇴한 사용자입니다");
        }

        String access = jwt.issueAccess(user.getId(), user.getPlan(), user.isOnboarded());
        String refresh = jwt.issueRefresh(user.getId());
        sessionRepository.save(AuthSession.create(
                user.getId(),
                TokenHasher.sha256(refresh),
                httpReq.getHeader("User-Agent"),
                httpReq.getRemoteAddr(),
                OffsetDateTime.now().plusSeconds(jwt.refreshTtlSeconds())
        ));

        return new AuthTokenResponse(
                access, refresh, jwt.accessTtlSeconds(), false,
                new AuthTokenResponse.UserSummary(user.getId(), user.getName(), user.getHandle(), user.isOnboarded())
        );
    }

    @Transactional
    public void logout(String refreshToken) {
        String hash = TokenHasher.sha256(refreshToken);
        sessionRepository.findByRefreshTokenHash(hash).ifPresent(s -> {
            if (!s.isRevoked()) s.revoke();
        });
    }
}
