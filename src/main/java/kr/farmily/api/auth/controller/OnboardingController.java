package kr.farmily.api.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.farmily.api.auth.dto.HandleCheckResponse;
import kr.farmily.api.auth.dto.OnboardingRequest;
import kr.farmily.api.auth.dto.OnboardingResponse;
import kr.farmily.api.auth.service.HandleSuggestionService;
import kr.farmily.api.auth.service.OnboardingService;
import kr.farmily.api.common.response.ApiResponse;
import kr.farmily.api.common.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Onboarding")
@RestController
@RequestMapping("/api/v1/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final HandleSuggestionService handleSuggestionService;

    @PostMapping
    @Operation(summary = "온보딩 (handle, farm, crops, location 한 번에)")
    public ApiResponse<OnboardingResponse> complete(CurrentUser user, @Valid @RequestBody OnboardingRequest req) {
        return ApiResponse.ok(onboardingService.complete(user.id(), req));
    }

    @GetMapping("/handle/check")
    @Operation(summary = "handle 사용 가능 여부 + 추천 5개")
    public ApiResponse<HandleCheckResponse> checkHandle(@RequestParam String handle) {
        boolean available = handleSuggestionService.isAvailable(handle);
        return ApiResponse.ok(new HandleCheckResponse(
                available,
                available ? java.util.List.of() : handleSuggestionService.suggest(handle)
        ));
    }
}
