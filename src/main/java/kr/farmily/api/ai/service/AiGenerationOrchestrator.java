package kr.farmily.api.ai.service;

import kr.farmily.api.ai.config.AiProperties;
import kr.farmily.api.ai.domain.ContentJob;
import kr.farmily.api.ai.domain.ContentResult;
import kr.farmily.api.ai.domain.JobStatus;
import kr.farmily.api.ai.domain.Platform;
import kr.farmily.api.ai.event.AiJobCreatedEvent;
import kr.farmily.api.ai.repository.ContentJobRepository;
import kr.farmily.api.ai.repository.ContentResultRepository;
import kr.farmily.api.diary.repository.DiaryPhotoRepository;
import kr.farmily.api.subscription.service.CreditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiGenerationOrchestrator {

    private final ContentJobRepository jobRepo;
    private final ContentResultRepository resultRepo;
    private final DiaryPhotoRepository diaryPhotoRepo;
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

            job.transition(JobStatus.ENRICHING, 40);
            job.transition(JobStatus.GENERATING, 70);

            List<String> photoKeys = buildPhotoKeys(job);
            BedrockAgentClient.Result r = bedrockClient.invoke(job, photoKeys);
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

    private List<String> buildPhotoKeys(ContentJob job) {
        List<String> result = new ArrayList<>();
        if (job.getExtraPhotoKeys() != null) {
            result.addAll(Arrays.asList(job.getExtraPhotoKeys()));
        }
        if (result.size() < 4 && job.getDiaryIds() != null && job.getDiaryIds().length > 0) {
            List<String> diaryKeys = diaryPhotoRepo.findS3KeysByDiaryIds(Arrays.asList(job.getDiaryIds()));
            for (String key : diaryKeys) {
                if (!result.contains(key)) {
                    result.add(key);
                }
                if (result.size() >= 4) break;
            }
        }
        return result;
    }

    @SuppressWarnings("unused")
    private Map<String, Object> baseMeta(ContentJob job) {
        Map<String, Object> m = new HashMap<>();
        m.put("provider", aiProps.provider());
        m.put("platform", job.getPlatform() == null ? Platform.INSTAGRAM.name() : job.getPlatform().name());
        return m;
    }
}
