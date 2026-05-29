package kr.farmily.api.profile.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.farmily.api.common.response.ApiResponse;
import kr.farmily.api.common.security.CurrentUser;
import kr.farmily.api.profile.dto.MyProfileResponse;
import kr.farmily.api.profile.dto.UpdateProfileRequest;
import kr.farmily.api.profile.service.ProfileEditService;
import kr.farmily.api.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Profile")
@RestController
@RequestMapping("/api/v1/me/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final ProfileEditService editService;

    @GetMapping
    @Operation(summary = "내 명함 조회")
    public ApiResponse<MyProfileResponse> get(CurrentUser user) {
        return ApiResponse.ok(profileService.getMyProfile(user.id()));
    }

    @PatchMapping
    @Operation(summary = "내 명함 편집")
    public ApiResponse<MyProfileResponse> update(CurrentUser user, @Valid @RequestBody UpdateProfileRequest req) {
        editService.update(user.id(), req);
        return ApiResponse.ok(profileService.getMyProfile(user.id()));
    }

    @GetMapping("/preview")
    @Operation(summary = "내 명함 미리보기 (소비자 시점)")
    public ApiResponse<MyProfileResponse> preview(CurrentUser user) {
        return ApiResponse.ok(profileService.getMyProfile(user.id()));
    }
}
