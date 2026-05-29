package kr.farmily.api.notification.service;

import kr.farmily.api.notification.domain.PushToken;
import kr.farmily.api.notification.repository.PushTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PushTokenService {

    private final PushTokenRepository tokenRepo;

    @Transactional
    public void register(long userId, String platform, String token) {
        tokenRepo.findByToken(token).ifPresentOrElse(
                PushToken::touch,
                () -> tokenRepo.save(PushToken.create(userId, platform, token))
        );
    }

    @Transactional
    public void delete(String token) {
        tokenRepo.deleteByToken(token);
    }
}
