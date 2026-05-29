package kr.farmily.api.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import kr.farmily.api.auth.client.KakaoOAuthClient;
import kr.farmily.api.auth.client.KakaoProfile;
import kr.farmily.api.auth.domain.AuthSession;
import kr.farmily.api.auth.dto.AuthTokenResponse;
import kr.farmily.api.auth.dto.KakaoLoginRequest;
import kr.farmily.api.auth.repository.AuthSessionRepository;
import kr.farmily.api.common.security.JwtTokenProvider;
import kr.farmily.api.user.domain.User;
import kr.farmily.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoOAuthClient kakaoClient;
    private final UserRepository userRepository;
    private final AuthSessionRepository sessionRepository;
    private final JwtTokenProvider jwt;

    @Transactional
    public AuthTokenResponse loginWithKakao(KakaoLoginRequest req, HttpServletRequest httpReq) {
        KakaoProfile profile = kakaoClient.fetchProfile(req.code(), req.redirectUri());

        boolean isNewUser;
        User user = userRepository.findByKakaoId(profile.kakaoId()).orElse(null);
        if (user == null) {
            user = userRepository.save(User.create(profile.kakaoId(), profile.nickname(), profile.email()));
            isNewUser = true;
        } else {
            if (user.isDeleted()) {
                user.restore();
            }
            isNewUser = !user.isOnboarded();
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
                access,
                refresh,
                jwt.accessTtlSeconds(),
                isNewUser,
                new AuthTokenResponse.UserSummary(user.getId(), user.getName(), user.getHandle(), user.isOnboarded())
        );
    }
}
