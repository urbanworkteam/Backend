package kr.farmily.api.auth.client;

import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Slf4j
@Component
public class KakaoOAuthClient {

    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String ME_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String UNLINK_URL = "https://kapi.kakao.com/v1/user/unlink";

    private final WebClient webClient;
    private final KakaoProperties props;

    public KakaoOAuthClient(KakaoProperties props) {
        this.props = props;
        this.webClient = WebClient.builder().build();
    }

    public KakaoProfile fetchProfile(String code, String redirectUri) {
        String accessToken = exchangeCodeForToken(code, redirectUri);
        return fetchMe(accessToken);
    }

    /**
     * 모바일 네이티브 SDK 가 이미 발급받은 카카오 액세스 토큰으로 프로필만 조회.
     * (code 교환 단계를 건너뜀 — client_secret 불필요)
     */
    public KakaoProfile fetchProfileByToken(String accessToken) {
        return fetchMe(accessToken);
    }

    private String exchangeCodeForToken(String code, String redirectUri) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", props.clientId());
        if (props.clientSecret() != null && !props.clientSecret().isBlank()) {
            form.add("client_secret", props.clientSecret());
        }
        form.add("redirect_uri", redirectUri != null ? redirectUri : props.redirectUri());
        form.add("code", code);

        try {
            Map<String, Object> body = webClient.post()
                    .uri(TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(form))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (body == null || body.get("access_token") == null) {
                throw new BusinessException(ErrorCode.KAKAO_API_ERROR, "카카오 토큰 응답 누락");
            }
            return body.get("access_token").toString();
        } catch (WebClientResponseException ex) {
            log.warn("Kakao token exchange failed: {}", ex.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.UNAUTHENTICATED, "카카오 인가코드가 유효하지 않습니다");
        } catch (Exception ex) {
            log.error("Kakao token exchange error", ex);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "카카오 인증 서버 오류");
        }
    }

    @SuppressWarnings("unchecked")
    private KakaoProfile fetchMe(String accessToken) {
        try {
            Map<String, Object> body = webClient.get()
                    .uri(ME_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            if (body == null || body.get("id") == null) {
                throw new BusinessException(ErrorCode.KAKAO_API_ERROR, "카카오 사용자 정보 응답 누락");
            }
            String kakaoId = body.get("id").toString();
            String nickname = null;
            String email = null;
            Map<String, Object> account = (Map<String, Object>) body.get("kakao_account");
            if (account != null) {
                email = (String) account.get("email");
                Map<String, Object> profile = (Map<String, Object>) account.get("profile");
                if (profile != null) {
                    nickname = (String) profile.get("nickname");
                }
            }
            return new KakaoProfile(kakaoId, nickname, email);
        } catch (WebClientResponseException ex) {
            log.warn("Kakao /me failed: {}", ex.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.KAKAO_API_ERROR, "카카오 사용자 조회 실패");
        } catch (Exception ex) {
            log.error("Kakao /me error", ex);
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR, "카카오 사용자 정보 서버 오류");
        }
    }

    public void unlink(String kakaoId) {
        if (props.adminKey() == null || props.adminKey().isBlank()) {
            log.warn("Kakao admin key 미설정. unlink skip (kakaoId={})", kakaoId);
            return;
        }
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("target_id_type", "user_id");
            form.add("target_id", kakaoId);

            webClient.post()
                    .uri(UNLINK_URL)
                    .header("Authorization", "KakaoAK " + props.adminKey())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(form))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception ex) {
            log.warn("Kakao unlink failed (continuing): kakaoId={}, err={}", kakaoId, ex.getMessage());
        }
    }
}
