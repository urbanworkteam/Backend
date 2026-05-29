package kr.farmily.api.profile.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.farmily.api.common.response.ApiResponse;
import kr.farmily.api.common.security.CurrentUser;
import kr.farmily.api.profile.dto.BlockReorderRequest;
import kr.farmily.api.profile.dto.CreateBlockRequest;
import kr.farmily.api.profile.service.ProfileBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Profile - Blocks")
@RestController
@RequestMapping("/api/v1/me/profile/blocks")
@RequiredArgsConstructor
public class ProfileBlockController {

    private final ProfileBlockService blockService;

    @PutMapping
    @Operation(summary = "블록 일괄 재정렬 / 토글")
    public ApiResponse<Void> reorder(CurrentUser user, @Valid @RequestBody BlockReorderRequest req) {
        blockService.reorder(user.id(), req);
        return ApiResponse.ok();
    }

    @PostMapping
    @Operation(summary = "TEXT 블록 추가")
    public ApiResponse<Long> create(CurrentUser user, @Valid @RequestBody CreateBlockRequest req) {
        return ApiResponse.ok(blockService.create(user.id(), req).getId());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "TEXT 블록 삭제")
    public ApiResponse<Void> delete(CurrentUser user, @PathVariable Long id) {
        blockService.delete(user.id(), id);
        return ApiResponse.ok();
    }
}
