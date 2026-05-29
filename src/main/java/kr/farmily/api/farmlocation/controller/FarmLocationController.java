package kr.farmily.api.farmlocation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.farmily.api.common.response.ApiResponse;
import kr.farmily.api.common.security.CurrentUser;
import kr.farmily.api.farmlocation.dto.FarmLocationPatchRequest;
import kr.farmily.api.farmlocation.dto.FarmLocationRequest;
import kr.farmily.api.farmlocation.dto.FarmLocationResponse;
import kr.farmily.api.farmlocation.service.FarmLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Farm Locations")
@RestController
@RequestMapping("/api/v1/farm-locations")
@RequiredArgsConstructor
public class FarmLocationController {

    private final FarmLocationService service;

    @GetMapping
    @Operation(summary = "내 농장 위치 목록")
    public ApiResponse<List<FarmLocationResponse>> list(CurrentUser user) {
        return ApiResponse.ok(service.listMine(user.id()).stream().map(FarmLocationResponse::from).toList());
    }

    @PostMapping
    @Operation(summary = "농장 위치 등록 (지오코딩 자동)")
    public ApiResponse<FarmLocationResponse> create(CurrentUser user, @Valid @RequestBody FarmLocationRequest req) {
        return ApiResponse.ok(FarmLocationResponse.from(service.create(user.id(), req)));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "농장 위치 수정")
    public ApiResponse<FarmLocationResponse> update(CurrentUser user, @PathVariable Long id,
                                                    @Valid @RequestBody FarmLocationPatchRequest req) {
        return ApiResponse.ok(FarmLocationResponse.from(service.update(user.id(), id, req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "농장 위치 삭제")
    public ApiResponse<Void> delete(CurrentUser user, @PathVariable Long id) {
        service.delete(user.id(), id);
        return ApiResponse.ok();
    }
}
