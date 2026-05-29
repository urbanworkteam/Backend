package kr.farmily.api.notification.dto;

public record NotificationSettingUpdateRequest(
        Boolean pushEnabled,
        Boolean diaryReminderEnabled,
        Boolean trendPushEnabled,
        Boolean marketingPushEnabled
) {}
