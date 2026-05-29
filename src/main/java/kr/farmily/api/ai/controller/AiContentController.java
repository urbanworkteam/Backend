package kr.farmily.api.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.farmily.api.ai.domain.Platform;
import kr.farmily.api.ai.dto.*;
import kr.farmily.api.ai.service.AiContentService;
import kr.farmily.api.ai.service.AiHistoryService;
import kr.farmily.api.ai.service.AiRegenerateService;
import kr.farmily.api.ai.service.AiResultService;
import kr.farmily.api.common.response.ApiResponse;
import kr.farmily.api.common.response.PageResponse;
import kr.farmily.api.common.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "AI Contents")
@RestController
@RequestMapping("/api/v1/ai/contents")
@RequiredArgsConstructor
public class AiContentController {

    private final AiContentService contentService;
    private final AiResultService resultService;
    private final AiHistoryService historyService;
    private final AiRegenerateService regenerateService;

    @PostMapping
    @Operation(summary = "AI 콘텐츠 생성 요청")
    public ApiResponse<JobCreatedResponse> create(CurrentUser user,
                                                  @RequestHeader(value = "X-Idempotency-Key", required = false) String idemKey,
                                                  @Valid @RequestBody CreateContentRequest req) {
        return ApiResponse.ok(contentService.create(user.id(), req, idemKey));
    }

    @GetMapping
    @Operation(summary = "내 AI 콘텐츠 이력 (커서 페이지네이션)")
    public ApiResponse<PageResponse<HistoryItemDto>> history(CurrentUser user,
                                                             @RequestParam(required = false) Platform platform,
                                                             @RequestParam(required = false) String cursor,
                                                             @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(historyService.list(user.id(), platform, cursor, Math.min(50, Math.max(1, limit))));
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "잡 상태 조회")
    public ApiResponse<JobStatusResponse> status(CurrentUser user, @PathVariable Long jobId) {
        return ApiResponse.ok(resultService.status(user.id(), jobId));
    }

    @GetMapping("/{jobId}/result")
    @Operation(summary = "결과 조회")
    public ApiResponse<ContentResultResponse> result(CurrentUser user, @PathVariable Long jobId) {
        return ApiResponse.ok(resultService.result(user.id(), jobId));
    }

    @PatchMapping("/{jobId}/result")
    @Operation(summary = "결과 캡션·해시태그 편집")
    public ApiResponse<Void> updateResult(CurrentUser user, @PathVariable Long jobId,
                                          @Valid @RequestBody UpdateResultRequest req) {
        resultService.updateResult(user.id(), jobId, req);
        return ApiResponse.ok();
    }

    @PostMapping("/{jobId}/downloads")
    @Operation(summary = "다운로드 카운트")
    public ApiResponse<Void> download(CurrentUser user, @PathVariable Long jobId,
                                      @RequestBody(required = false) Map<String, String> body) {
        resultService.recordDownload(user.id(), jobId);
        return ApiResponse.ok();
    }

    @PostMapping("/{jobId}/regenerate")
    @Operation(summary = "재생성 (24h 3회 무료)")
    public ApiResponse<RegenerateResponse> regenerate(CurrentUser user, @PathVariable Long jobId,
                                                      @Valid @RequestBody RegenerateRequest req) {
        return ApiResponse.ok(regenerateService.regenerate(user.id(), jobId, req));
    }
}
