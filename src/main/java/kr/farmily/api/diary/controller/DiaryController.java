package kr.farmily.api.diary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.farmily.api.common.response.ApiResponse;
import kr.farmily.api.common.response.PageResponse;
import kr.farmily.api.common.security.CurrentUser;
import kr.farmily.api.diary.domain.WorkType;
import kr.farmily.api.diary.dto.DiaryResponse;
import kr.farmily.api.diary.dto.WriteDiaryRequest;
import kr.farmily.api.diary.service.DiaryDeleteService;
import kr.farmily.api.diary.service.DiaryListService;
import kr.farmily.api.diary.service.DiaryReadService;
import kr.farmily.api.diary.service.DiaryUpdateService;
import kr.farmily.api.diary.service.DiaryWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "Diary")
@RestController
@RequestMapping("/api/v1/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryWriteService writeService;
    private final DiaryReadService readService;
    private final DiaryListService listService;
    private final DiaryUpdateService updateService;
    private final DiaryDeleteService deleteService;

    @PostMapping
    @Operation(summary = "영농일지 작성")
    public ApiResponse<DiaryResponse> write(CurrentUser user, @Valid @RequestBody WriteDiaryRequest req) {
        return ApiResponse.ok(writeService.write(user.id(), req));
    }

    @GetMapping
    @Operation(summary = "작물별 기간 영농일지 목록",
            description = "특정 작물의 일정 기간 일지를 최신순(diary_date DESC, id DESC) 으로 커서 페이지네이션. "
                    + "fromDate/toDate 미지정 시 오늘 기준 최근 90일.")
    public ApiResponse<PageResponse<DiaryResponse>> list(CurrentUser user,
                                                         @RequestParam Long cropId,
                                                         @RequestParam(required = false)
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                         @RequestParam(required = false)
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                                         @RequestParam(required = false) String cursor,
                                                         @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(listService.listByCrop(user.id(), cropId, fromDate, toDate, cursor, limit));
    }

    @GetMapping("/{id}")
    @Operation(summary = "영농일지 단건 조회")
    public ApiResponse<DiaryResponse> read(CurrentUser user, @PathVariable Long id) {
        return ApiResponse.ok(readService.findById(user.id(), id));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "영농일지 수정 (전체 교체식)")
    public ApiResponse<DiaryResponse> update(CurrentUser user, @PathVariable Long id,
                                             @Valid @RequestBody WriteDiaryRequest req) {
        return ApiResponse.ok(updateService.update(user.id(), id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "영농일지 삭제 (소프트)")
    public ApiResponse<Void> delete(CurrentUser user, @PathVariable Long id) {
        deleteService.delete(user.id(), id);
        return ApiResponse.ok();
    }

    @GetMapping("/work-types")
    @Operation(summary = "작업 유형 마스터 7종")
    public ApiResponse<List<Map<String, String>>> workTypes() {
        return ApiResponse.ok(WorkType.all().stream()
                .map(w -> Map.of("code", w.name(), "label", w.label, "icon", w.icon))
                .toList());
    }
}
