package kr.farmily.api.ai.dto;

import kr.farmily.api.ai.domain.JobStatus;

import java.util.List;

public record JobStatusResponse(
        Long id,
        JobStatus status,
        int progressPct,
        List<Step> steps,
        String failureReason
) {

    public record Step(String key, String label, boolean done) {}
}
