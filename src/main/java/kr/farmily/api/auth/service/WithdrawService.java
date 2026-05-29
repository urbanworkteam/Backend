package kr.farmily.api.auth.service;

import kr.farmily.api.auth.client.KakaoOAuthClient;
import kr.farmily.api.auth.domain.DeletionRequest;
import kr.farmily.api.auth.repository.AuthSessionRepository;
import kr.farmily.api.auth.repository.DeletionRequestRepository;
import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import kr.farmily.api.user.domain.User;
import kr.farmily.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class WithdrawService {

    private static final int GRACE_DAYS = 30;

    private final UserRepository userRepository;
    private final AuthSessionRepository sessionRepository;
    private final DeletionRequestRepository deletionRepository;
    private final KakaoOAuthClient kakaoClient;

    @Transactional
    public OffsetDateTime withdraw(long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.isDeleted()) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "이미 탈퇴한 사용자입니다");
        }

        user.softDelete();
        sessionRepository.revokeAllForUser(userId, OffsetDateTime.now());

        DeletionRequest req = deletionRepository.findByUserId(userId)
                .orElseGet(() -> DeletionRequest.open(userId, GRACE_DAYS));
        deletionRepository.save(req);

        kakaoClient.unlink(user.getKakaoId());

        return req.getPurgeAt();
    }

    @Transactional
    public void cancelWithdraw(long userId) {
        DeletionRequest req = deletionRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "탈퇴 요청이 없습니다"));
        if (req.getPurgeAt().isBefore(OffsetDateTime.now())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "유예기간이 만료되었습니다");
        }
        req.cancel();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        user.restore();
    }
}
