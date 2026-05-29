package kr.farmily.api.profile.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.farmily.api.common.response.ApiResponse;
import kr.farmily.api.common.security.CurrentUser;
import kr.farmily.api.diary.dto.CalendarMonthResponse;
import kr.farmily.api.diary.dto.DiaryResponse;
import kr.farmily.api.profile.service.ProfileCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Profile - Calendar")
@RestController
@RequestMapping("/api/v1/me/profile/calendar")
@RequiredArgsConstructor
public class ProfileCalendarController {

    private final ProfileCalendarService service;

    @GetMapping
    @Operation(summary = "명함 달력 월별 태그")
    public ApiResponse<CalendarMonthResponse> month(CurrentUser user,
                                                    @RequestParam int year,
                                                    @RequestParam int month) {
        return ApiResponse.ok(service.getMonth(user.id(), year, month));
    }

    @GetMapping("/{date}")
    @Operation(summary = "명함 달력 일별 인라인 카드")
    public ApiResponse<List<DiaryResponse>> day(CurrentUser user,
                                                @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(service.getDay(user.id(), date));
    }
}
