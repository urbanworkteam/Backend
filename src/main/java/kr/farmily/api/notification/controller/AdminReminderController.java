package kr.farmily.api.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.farmily.api.common.response.ApiResponse;
import kr.farmily.api.notification.service.ReminderLoadTestService;
import kr.farmily.api.notification.service.ReminderLoadTestService.RunResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 야간 push 배치 부하 실험 트리거. farmily.experiment.enabled=false 이면 404 로 숨긴다.
 * SecurityConfig 가 /api/v1/admin/** 를 anyRequest().authenticated() 로 보호하므로 JWT 필요.
 */
@Tag(name = "Admin Reminder (Load Test)")
@RestController
@RequestMapping("/api/v1/admin/reminder")
@RequiredArgsConstructor
public class AdminReminderController {

    @Value("${farmily.experiment.enabled:false}")
    private boolean experimentEnabled;

    private final ReminderLoadTestService service;

    @PostMapping("/run")
    @Operation(summary = "리마인더 발송 부하 실험 실행 (experiment.enabled=true 시에만)")
    public ApiResponse<RunResult> run(
            @RequestParam(defaultValue = "single") String mode,
            @RequestParam(defaultValue = "16") int threads,
            @RequestParam(defaultValue = "0") int limit,
            @RequestParam(defaultValue = "500000") long target) {
        if (!experimentEnabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ApiResponse.ok(service.run(mode, threads, limit, target));
    }
}
