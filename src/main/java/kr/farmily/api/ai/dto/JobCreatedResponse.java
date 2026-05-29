package kr.farmily.api.ai.dto;

import kr.farmily.api.ai.domain.JobStatus;

public record JobCreatedResponse(
        Long jobId,
        JobStatus status,
        int creditsRemaining
) {}
