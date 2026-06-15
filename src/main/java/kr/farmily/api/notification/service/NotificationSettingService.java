package kr.farmily.api.notification.service;

import kr.farmily.api.notification.domain.NotificationSetting;
import kr.farmily.api.notification.dto.NotificationSettingResponse;
import kr.farmily.api.notification.dto.NotificationSettingUpdateRequest;
import kr.farmily.api.notification.repository.NotificationSettingRepository;
import kr.farmily.api.common.cache.CacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationSettingService {

    private final NotificationSettingRepository repo;

    @Cacheable(cacheNames = CacheNames.NOTIFICATION_SETTINGS, key = "#userId")
    @Transactional
    public NotificationSettingResponse get(long userId) {
        NotificationSetting s = repo.findById(userId).orElseGet(() -> repo.save(NotificationSetting.createDefault(userId)));
        return toResponse(s);
    }

    @CacheEvict(cacheNames = CacheNames.NOTIFICATION_SETTINGS, key = "#userId")
    @Transactional
    public NotificationSettingResponse update(long userId, NotificationSettingUpdateRequest req) {
        NotificationSetting s = repo.findById(userId).orElseGet(() -> repo.save(NotificationSetting.createDefault(userId)));
        s.update(req.pushEnabled(), req.diaryReminderEnabled(), req.trendPushEnabled(), req.marketingPushEnabled());
        return toResponse(s);
    }

    private NotificationSettingResponse toResponse(NotificationSetting s) {
        return new NotificationSettingResponse(
                s.isPushEnabled(), s.isDiaryReminderEnabled(),
                s.isTrendPushEnabled(), s.isMarketingPushEnabled()
        );
    }
}
