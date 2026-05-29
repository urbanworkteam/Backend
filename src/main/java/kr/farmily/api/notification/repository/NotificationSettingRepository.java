package kr.farmily.api.notification.repository;

import kr.farmily.api.notification.domain.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {}
