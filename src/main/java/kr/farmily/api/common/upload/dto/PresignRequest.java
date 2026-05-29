package kr.farmily.api.common.upload.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.farmily.api.common.upload.UploadKind;

public record PresignRequest(
        @NotNull UploadKind kind,
        @NotBlank String ext,
        @Min(1) long sizeBytes
) {}
