package kr.farmily.api.ai.dto;

import kr.farmily.api.ai.domain.JobStatus;

import java.time.OffsetDateTime;

public record RegenerateResponse(
        Long jobId,
        JobStatus status,
        boolean creditsCharged,
        Regeneration regeneration
) {

    public record Regeneration(int count, int freeRemaining, OffsetDateTime windowEndsAt) {}
}
