package kr.farmily.api.profile.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.farmily.api.common.response.ApiResponse;
import kr.farmily.api.diary.dto.CalendarMonthResponse;
import kr.farmily.api.diary.dto.DiaryResponse;
import kr.farmily.api.diary.service.DiaryCalendarService;
import kr.farmily.api.diary.service.DiaryReadService;
import kr.farmily.api.diary.repository.FarmDiaryRepository;
import kr.farmily.api.profile.dto.PublicProfileResponse;
import kr.farmily.api.profile.service.PublicProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "Public Profile")
@RestController
@RequestMapping("/api/v1/public/farms")
@RequiredArgsConstructor
public class PublicProfileController {

    private final PublicProfileService publicProfileService;
    private final DiaryCalendarService diaryCalendarService;
    private final DiaryReadService diaryReadService;
    private final FarmDiaryRepository diaryRepository;

    @GetMapping("/{handle}")
    @Operation(summary = "소비자 명함 조회")
    public ResponseEntity<ApiResponse<PublicProfileResponse>> get(@PathVariable String handle) {
        PublicProfileResponse res = publicProfileService.findByHandle(handle);
        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofSeconds(60)).cachePublic())
                .body(ApiResponse.ok(res));
    }

    @GetMapping("/{handle}/exists")
    @Operation(summary = "handle 존재 여부")
    public ApiResponse<Map<String, Object>> exists(@PathVariable String handle) {
        return ApiResponse.ok(Map.of("exists", publicProfileService.handleExists(handle)));
    }

    @GetMapping("/{handle}/calendar")
    @Operation(summary = "소비자 달력 (월별)")
    public ApiResponse<CalendarMonthResponse> calendar(@PathVariable String handle,
                                                       @RequestParam int year,
                                                       @RequestParam int month) {
        long userId = publicProfileService.resolveUserId(handle);
        return ApiResponse.ok(diaryCalendarService.getMonth(userId, year, month));
    }

    @GetMapping("/{handle}/calendar/{date}")
    @Operation(summary = "소비자 달력 (일별)")
    public ApiResponse<List<DiaryResponse>> day(@PathVariable String handle,
                                                @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        long userId = publicProfileService.resolveUserId(handle);
        return ApiResponse.ok(diaryRepository.findActiveInRange(userId, date, date).stream()
                .map(d -> diaryReadService.findById(userId, d.getId())).toList());
    }
}
