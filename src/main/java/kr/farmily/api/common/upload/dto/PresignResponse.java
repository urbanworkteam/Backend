package kr.farmily.api.common.upload.dto;

public record PresignResponse(
        String uploadUrl,
        String key,
        String publicUrl,
        long expiresIn
) {}
