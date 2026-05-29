package kr.farmily.api.notification.dto;

public record NotificationSettingResponse(
        boolean pushEnabled,
        boolean diaryReminderEnabled,
        boolean trendPushEnabled,
        boolean marketingPushEnabled
) {}
