package kr.farmily.api.ai.service;

import kr.farmily.api.ai.domain.ContentJob;
import kr.farmily.api.ai.domain.JobStatus;
import kr.farmily.api.ai.dto.RegenerateRequest;
import kr.farmily.api.ai.dto.RegenerateResponse;
import kr.farmily.api.ai.event.AiJobCreatedEvent;
import kr.farmily.api.ai.repository.ContentJobRepository;
import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import kr.farmily.api.common.upload.PhotoKeyValidator;
import kr.farmily.api.subscription.service.CreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiRegenerateService {

    private static final int FREE_LIMIT = 3;

    private final ContentJobRepository jobRepo;
    private final CreditService creditService;
    private final PhotoKeyValidator photoKeyValidator;
    private final ApplicationEventPublisher events;

    @Transactional
    public RegenerateResponse regenerate(long userId, long jobId, RegenerateRequest req) {
        ContentJob original = jobRepo.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.JOB_NOT_FOUND));
        if (original.getStatus() == JobStatus.FAILED) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "실패한 잡은 재생성할 수 없습니다");
        }
        photoKeyValidator.requireAllOwnedBy(userId, req.extraPhotoKeys());

        long rootId = original.getRegeneratedFrom() != null ? original.getRegeneratedFrom() : original.getId();
        OffsetDateTime windowStart = OffsetDateTime.now().minusHours(24);
        long count = jobRepo.countRegenerationsInWindow(userId, rootId, windowStart);

        String idemKey = UUID.randomUUID().toString();
        boolean charged = count >= FREE_LIMIT;
        if (charged) creditService.tryConsume(userId, idemKey);

        String[] extra = req.extraPhotoKeys() == null ? null : req.extraPhotoKeys().toArray(String[]::new);
        String keywords = req.keywords() != null ? req.keywords() : original.getKeywords();

        ContentJob newJob = jobRepo.save(ContentJob.create(
                userId, original.getPlatform(), original.getCropId(),
                original.getDiaryIds(), keywords, extra, charged, rootId
        ));

        events.publishEvent(new AiJobCreatedEvent(newJob.getId(), idemKey));

        int freeRemaining = Math.max(0, FREE_LIMIT - (int) count - 1);
        return new RegenerateResponse(
                newJob.getId(), newJob.getStatus(), charged,
                new RegenerateResponse.Regeneration((int) count + 1, freeRemaining, windowStart.plusHours(24))
        );
    }
}
