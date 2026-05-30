package kr.farmily.api.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import kr.farmily.api.auth.client.KakaoOAuthClient;
import kr.farmily.api.auth.client.KakaoProfile;
import kr.farmily.api.auth.config.AuthDevMasterProperties;
import kr.farmily.api.auth.domain.AuthSession;
import kr.farmily.api.auth.dto.AuthTokenResponse;
import kr.farmily.api.auth.dto.KakaoLoginRequest;
import kr.farmily.api.auth.repository.AuthSessionRepository;
import kr.farmily.api.common.security.JwtTokenProvider;
import kr.farmily.api.user.domain.User;
import kr.farmily.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoOAuthClient kakaoClient;
    private final UserRepository userRepository;
    private final AuthSessionRepository sessionRepository;
    private final JwtTokenProvider jwt;
    private final AuthDevMasterProperties devMaster;

    @Transactional
    public AuthTokenResponse loginWithKakao(KakaoLoginRequest req, HttpServletRequest httpReq) {
        if (devMaster.enabled() && devMaster.code().equals(req.code())) {
            log.warn("[DEV-MASTER] Bypassing Kakao OAuth — should NEVER appear in production logs");
            return issueForUser(resolveDevMasterUser(), httpReq);
        }

        KakaoProfile profile = kakaoClient.fetchProfile(req.code(), req.redirectUri());

        User user = userRepository.findByKakaoId(profile.kakaoId()).orElse(null);
        if (user == null) {
            user = userRepository.save(User.create(profile.kakaoId(), profile.nickname(), profile.email()));
        } else if (user.isDeleted()) {
            user.restore();
        }
        return issueForUser(user, httpReq);
    }

    private User resolveDevMasterUser() {
        return userRepository.findByKakaoId(devMaster.kakaoId())
                .map(u -> {
                    if (u.isDeleted()) u.restore();
                    return u;
                })
                .orElseGet(() -> userRepository.save(
                        User.create(devMaster.kakaoId(), devMaster.nickname(), devMaster.email())));
    }

    private AuthTokenResponse issueForUser(User user, HttpServletRequest httpReq) {
        boolean isNewUser = !user.isOnboarded();
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
                access,
                refresh,
                jwt.accessTtlSeconds(),
                isNewUser,
                new AuthTokenResponse.UserSummary(user.getId(), user.getName(), user.getHandle(), user.isOnboarded())
        );
    }
}
