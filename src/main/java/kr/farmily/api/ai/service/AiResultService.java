package kr.farmily.api.ai.service;

import kr.farmily.api.ai.domain.ContentJob;
import kr.farmily.api.ai.domain.ContentResult;
import kr.farmily.api.ai.domain.JobStatus;
import kr.farmily.api.ai.dto.ContentResultResponse;
import kr.farmily.api.ai.dto.JobStatusResponse;
import kr.farmily.api.ai.dto.UpdateResultRequest;
import kr.farmily.api.ai.repository.ContentJobRepository;
import kr.farmily.api.ai.repository.ContentResultRepository;
import kr.farmily.api.common.config.S3Properties;
import kr.farmily.api.common.exception.BusinessException;
import kr.farmily.api.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiResultService {

    private final ContentJobRepository jobRepo;
    private final ContentResultRepository resultRepo;
    private final S3Properties s3Properties;

    @Transactional(readOnly = true)
    public JobStatusResponse status(long userId, long jobId) {
        ContentJob job = requireOwner(userId, jobId);
        return new JobStatusResponse(
                job.getId(), job.getStatus(),
                job.getProgressPct() == null ? 0 : job.getProgressPct(),
                List.of(
                        new JobStatusResponse.Step("ANALYZE_DIARY", "영농일지 데이터 분석",
                                job.getStatus().ordinal() >= JobStatus.ANALYZING.ordinal()),
                        new JobStatusResponse.Step("FETCH_SEASON", "제철 정보 조회",
                                job.getStatus().ordinal() >= JobStatus.ENRICHING.ordinal()),
                        new JobStatusResponse.Step("GENERATE_CONTENT", "콘텐츠 생성 중",
                                job.getStatus().ordinal() >= JobStatus.GENERATING.ordinal())
                ),
                job.getFailureReason()
        );
    }

    @Transactional(readOnly = true)
    public ContentResultResponse result(long userId, long jobId) {
        ContentJob job = requireOwner(userId, jobId);
        if (job.getStatus() != JobStatus.DONE) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "결과가 아직 준비되지 않았습니다");
        }
        ContentResult r = resultRepo.findById(jobId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "결과를 찾을 수 없습니다"));

        List<String> urls = Arrays.stream(r.getCardImageKeys()).map(this::toCdnUrl).toList();
        return new ContentResultResponse(job.getPlatform(), urls, r.getCaption(),
                r.getHashtags() == null ? List.of() : List.of(r.getHashtags()));
    }

    @Transactional
    public void updateResult(long userId, long jobId, UpdateResultRequest req) {
        requireOwner(userId, jobId);
        ContentResult r = resultRepo.findById(jobId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "결과 없음"));
        r.editCaption(req.caption());
        if (req.hashtags() != null) r.editHashtags(req.hashtags().toArray(String[]::new));
    }

    @Transactional
    public void recordDownload(long userId, long jobId) {
        requireOwner(userId, jobId);
        // 통계 카운터 별도 테이블 추가 시 여기서 INSERT. 본 spec 범위 외.
    }

    public ContentJob requireOwner(long userId, long jobId) {
        return jobRepo.findByIdAndUserId(jobId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.JOB_NOT_FOUND));
    }

    private String toCdnUrl(String key) {
        if (s3Properties.cdnBaseUrl() == null || s3Properties.cdnBaseUrl().isBlank()) return key;
        return s3Properties.cdnBaseUrl().replaceAll("/$", "") + "/" + key;
    }
}
