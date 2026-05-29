package kr.farmily.api.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.farmily.api.common.response.ApiResponse;
import kr.farmily.api.common.security.CurrentUser;
import kr.farmily.api.notification.dto.NotificationSettingResponse;
import kr.farmily.api.notification.dto.NotificationSettingUpdateRequest;
import kr.farmily.api.notification.service.NotificationSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification Settings")
@RestController
@RequestMapping("/api/v1/notification-settings")
@RequiredArgsConstructor
public class NotificationSettingController {

    private final NotificationSettingService service;

    @GetMapping
    @Operation(summary = "내 알림 설정 조회")
    public ApiResponse<NotificationSettingResponse> get(CurrentUser user) {
        return ApiResponse.ok(service.get(user.id()));
    }

    @PatchMapping
    @Operation(summary = "알림 설정 수정")
    public ApiResponse<NotificationSettingResponse> update(CurrentUser user,
                                                           @RequestBody NotificationSettingUpdateRequest req) {
        return ApiResponse.ok(service.update(user.id(), req));
    }
}
