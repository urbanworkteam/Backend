package kr.farmily.api.diary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.farmily.api.common.response.ApiResponse;
import kr.farmily.api.common.security.CurrentUser;
import kr.farmily.api.diary.dto.CalendarMonthResponse;
import kr.farmily.api.diary.service.DiaryCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Diary Calendar")
@RestController
@RequestMapping("/api/v1/diaries/calendar")
@RequiredArgsConstructor
public class DiaryCalendarController {

    private final DiaryCalendarService service;

    @GetMapping
    @Operation(summary = "월별 영농일지 색상 태그")
    public ApiResponse<CalendarMonthResponse> month(CurrentUser user,
                                                    @RequestParam int year,
                                                    @RequestParam int month) {
        return ApiResponse.ok(service.getMonth(user.id(), year, month));
    }
}
