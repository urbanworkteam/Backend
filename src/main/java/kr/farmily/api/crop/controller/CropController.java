package kr.farmily.api.crop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.farmily.api.common.response.ApiResponse;
import kr.farmily.api.common.security.CurrentUser;
import kr.farmily.api.crop.dto.CropPatchRequest;
import kr.farmily.api.crop.dto.CropRequest;
import kr.farmily.api.crop.dto.CropResponse;
import kr.farmily.api.crop.service.CropService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Crops")
@RestController
@RequestMapping("/api/v1/crops")
@RequiredArgsConstructor
public class CropController {

    private final CropService cropService;

    @GetMapping
    @Operation(summary = "내 재배 작물 목록")
    public ApiResponse<List<CropResponse>> list(CurrentUser user) {
        return ApiResponse.ok(cropService.listMine(user.id()).stream().map(CropResponse::from).toList());
    }

    @PostMapping
    @Operation(summary = "작물 등록")
    public ApiResponse<CropResponse> create(CurrentUser user, @Valid @RequestBody CropRequest req) {
        return ApiResponse.ok(CropResponse.from(cropService.create(user.id(), req)));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "작물 수정")
    public ApiResponse<CropResponse> update(CurrentUser user, @PathVariable Long id,
                                            @Valid @RequestBody CropPatchRequest req) {
        return ApiResponse.ok(CropResponse.from(cropService.update(user.id(), id, req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "작물 삭제 (소프트)")
    public ApiResponse<Void> delete(CurrentUser user, @PathVariable Long id) {
        cropService.delete(user.id(), id);
        return ApiResponse.ok();
    }
}
