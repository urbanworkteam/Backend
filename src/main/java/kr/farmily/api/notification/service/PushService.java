package kr.farmily.api.notification.service;

import kr.farmily.api.notification.client.FcmClient;
import kr.farmily.api.notification.domain.NotificationSetting;
import kr.farmily.api.notification.domain.PushToken;
import kr.farmily.api.notification.repository.NotificationSettingRepository;
import kr.farmily.api.notification.repository.PushTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PushService {

    private final PushTokenRepository tokenRepo;
    private final NotificationSettingRepository settingRepo;
    private final FcmClient fcmClient;

    @Transactional(readOnly = true)
    public void send(long userId, String title, String body, String deepLink) {
        NotificationSetting s = settingRepo.findById(userId).orElse(null);
        if (s != null && !s.isPushEnabled()) return;
        List<String> tokens = tokenRepo.findByUserId(userId).stream().map(PushToken::getToken).toList();
        if (tokens.isEmpty()) return;
        fcmClient.sendMulticast(tokens, title, body, deepLink);
    }

    @Transactional(readOnly = true)
    public boolean isReminderEnabled(long userId) {
        NotificationSetting s = settingRepo.findById(userId).orElse(null);
        return s == null || (s.isPushEnabled() && s.isDiaryReminderEnabled());
    }
}
