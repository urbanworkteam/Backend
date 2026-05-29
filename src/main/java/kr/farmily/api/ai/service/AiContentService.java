package kr.farmily.api.ai.service;

import kr.farmily.api.ai.domain.ContentJob;
import kr.farmily.api.ai.domain.Platform;
import kr.farmily.api.ai.dto.CreateContentRequest;
import kr.farmily.api.ai.dto.JobCreatedResponse;
import kr.farmily.api.ai.repository.ContentJobRepository;
import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import kr.farmily.api.common.upload.PhotoKeyValidator;
import kr.farmily.api.crop.service.CropService;
import kr.farmily.api.diary.domain.FarmDiary;
import kr.farmily.api.diary.repository.FarmDiaryRepository;
import kr.farmily.api.subscription.service.CreditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiContentService {

    private final ContentJobRepository jobRepo;
    private final CropService cropService;
    private final FarmDiaryRepository diaryRepo;
    private final PhotoKeyValidator photoKeyValidator;
    private final CreditService creditService;
    private final AiGenerationOrchestrator orchestrator;

    @Transactional
    public JobCreatedResponse create(long userId, CreateContentRequest req, String idemKey) {
        String key = idemKey != null && !idemKey.isBlank() ? idemKey : UUID.randomUUID().toString();

        cropService.requireOwner(userId, req.cropId());
        photoKeyValidator.requireAllOwnedBy(userId, req.extraPhotoKeys());

        Long[] diaryIds = null;
        if (req.diaryIds() != null && !req.diaryIds().isEmpty()) {
            List<FarmDiary> ds = diaryRepo.findActiveByIdsAndUser(userId, req.diaryIds());
            if (ds.size() != req.diaryIds().size()) {
                throw new BusinessException(ErrorCode.NOT_RESOURCE_OWNER,
                        "다른 사용자의 일지가 포함되어 있습니다", "diaryIds");
            }
            diaryIds = req.diaryIds().toArray(Long[]::new);
        }

        creditService.tryConsume(userId, key);

        String[] extra = req.extraPhotoKeys() == null ? null : req.extraPhotoKeys().toArray(String[]::new);
        ContentJob job = jobRepo.save(ContentJob.create(
                userId, req.platform(), req.cropId(), diaryIds, req.keywords(), extra, true, null
        ));

        orchestrator.run(job.getId(), key);

        int remaining = creditService.getStatus(userId).creditsRemaining();
        return new JobCreatedResponse(job.getId(), job.getStatus(), remaining);
    }

    @SuppressWarnings("unused")
    private static Platform noop() { return Platform.INSTAGRAM; }
}
