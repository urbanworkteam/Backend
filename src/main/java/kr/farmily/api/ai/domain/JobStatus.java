package kr.farmily.api.ai.domain;

public enum JobStatus {
    QUEUED,
    ANALYZING,
    ENRICHING,
    GENERATING,
    DONE,
    FAILED,
    REFUNDED;

    public boolean isTerminal() {
        return this == DONE || this == FAILED || this == REFUNDED;
    }
}
