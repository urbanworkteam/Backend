package kr.farmily.api.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.farmily.api.common.response.ApiResponse;
import kr.farmily.api.common.security.CurrentUser;
import kr.farmily.api.user.dto.MyPageResponse;
import kr.farmily.api.user.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "MyPage")
@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService service;

    @GetMapping
    @Operation(summary = "마이페이지 메인 조회")
    public ApiResponse<MyPageResponse> get(CurrentUser user) {
        return ApiResponse.ok(service.getMyPage(user.id()));
    }
}
