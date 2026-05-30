package kr.farmily.api.ai.service;

import kr.farmily.api.ai.config.AiProperties;
import kr.farmily.api.ai.domain.ContentJob;
import kr.farmily.api.ai.domain.ContentResult;
import kr.farmily.api.ai.domain.JobStatus;
import kr.farmily.api.ai.domain.Platform;
import kr.farmily.api.ai.event.AiJobCreatedEvent;
import kr.farmily.api.ai.repository.ContentJobRepository;
import kr.farmily.api.ai.repository.ContentResultRepository;
import kr.farmily.api.subscription.service.CreditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiGenerationOrchestrator {

    private final ContentJobRepository jobRepo;
    private final ContentResultRepository resultRepo;
    private final DiarySummaryBuilder summaryBuilder;
    private final CreditService creditService;
    private final BedrockAgentClient bedrockClient;
    private final AiProperties aiProps;

    /**
     * 잡 생성 트랜잭션이 커밋된 뒤에 비동기로 실행. 호출 측은 직접 invoke 하지 않고
     * AiJobCreatedEvent 를 발행만 함. AFTER_COMMIT 이라 이전의
     * "outer @Transactional 미커밋 -> findById 실패" race 가 발생할 수 없음.
     */
    @Async("aiTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onJobCreated(AiJobCreatedEvent event) {
        long jobId = event.jobId();
        String idempotencyKey = event.idempotencyKey();

        ContentJob job = jobRepo.findById(jobId).orElse(null);
        if (job == null) {
            log.warn("AI job not found in AFTER_COMMIT listener: jobId={}", jobId);
            return;
        }
        try {
            job.transition(JobStatus.ANALYZING, 20);
            String summary = summaryBuilder.build(job.getUserId(), job.getDiaryIds());

            job.transition(JobStatus.ENRICHING, 40);
            job.transition(JobStatus.GENERATING, 70);

            BedrockAgentClient.Result r = bedrockClient.invoke(job, summary);
            resultRepo.save(ContentResult.create(job.getId(),
                    r.cardImageKeys(), r.caption(), r.hashtags(), r.meta()));

            job.transition(JobStatus.DONE, 100);
        } catch (Exception e) {
            log.error("AI generation failed jobId={}", jobId, e);
            job.fail(e.getMessage());
            if (Boolean.TRUE.equals(job.getCreditCharged())) {
                try {
                    creditService.refund(job.getUserId(), idempotencyKey);
                    job.markRefunded();
                } catch (Exception ignored) {}
            }
        }
    }

    @SuppressWarnings("unused")
    private Map<String, Object> baseMeta(ContentJob job) {
        Map<String, Object> m = new HashMap<>();
        m.put("provider", aiProps.provider());
        m.put("platform", job.getPlatform() == null ? Platform.INSTAGRAM.name() : job.getPlatform().name());
        return m;
    }
}
