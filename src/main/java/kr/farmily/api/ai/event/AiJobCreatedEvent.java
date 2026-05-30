package kr.farmily.api.ai.event;

public record AiJobCreatedEvent(long jobId, String idempotencyKey) {}
