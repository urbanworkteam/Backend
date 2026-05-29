package kr.farmily.api.common.upload;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.farmily.api.common.response.ApiResponse;
import kr.farmily.api.common.security.CurrentUser;
import kr.farmily.api.common.upload.dto.PresignRequest;
import kr.farmily.api.common.upload.dto.PresignResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Uploads")
@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final PresignService presignService;

    @PostMapping("/presign")
    @Operation(summary = "S3 PUT presigned URL 발급")
    public ApiResponse<PresignResponse> presign(CurrentUser user, @Valid @RequestBody PresignRequest req) {
        return ApiResponse.ok(presignService.presign(user.id(), req));
    }
}
