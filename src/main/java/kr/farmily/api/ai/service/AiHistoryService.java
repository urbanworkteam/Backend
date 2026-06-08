package kr.farmily.api.ai.service;

import kr.farmily.api.ai.domain.ContentJob;
import kr.farmily.api.ai.domain.ContentResult;
import kr.farmily.api.ai.domain.JobStatus;
import kr.farmily.api.ai.domain.Platform;
import kr.farmily.api.ai.dto.HistoryItemDto;
import kr.farmily.api.ai.repository.ContentJobRepository;
import kr.farmily.api.ai.repository.ContentResultRepository;
import kr.farmily.api.common.response.PageResponse;
import kr.farmily.api.common.upload.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiHistoryService {

    private final ContentJobRepository jobRepo;
    private final ContentResultRepository resultRepo;
    private final S3Service s3Service;

    @Transactional(readOnly = true)
    public PageResponse<HistoryItemDto> list(long userId, Platform platform, String cursor, int limit) {
        OffsetDateTime cursorAt = decode(cursor);
        List<ContentJob> jobs = jobRepo.findHistoryPage(userId, platform, cursorAt, JobStatus.FAILED,
                PageRequest.of(0, limit + 1));
        boolean hasMore = jobs.size() > limit;
        List<ContentJob> page = hasMore ? jobs.subList(0, limit) : jobs;

        List<HistoryItemDto> data = page.stream().map(j -> {
            ContentResult r = resultRepo.findById(j.getId()).orElse(null);
            String thumb = (r != null && r.getCardImageKeys() != null && r.getCardImageKeys().length > 0)
                    ? s3Service.toDisplayUrl(r.getCardImageKeys()[0]) : null;
            String caption = r != null ? r.getCaption() : null;
            return new HistoryItemDto(j.getId(), j.getPlatform(), j.getCreatedAt(), thumb, caption);
        }).toList();
        String next = hasMore ? encode(page.get(page.size() - 1).getCreatedAt()) : null;
        return PageResponse.of(data, next, hasMore);
    }

    private String encode(OffsetDateTime at) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(at.toString().getBytes(StandardCharsets.UTF_8));
    }

    private OffsetDateTime decode(String cursor) {
        if (cursor == null || cursor.isBlank()) return null;
        try {
            String s = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            return OffsetDateTime.parse(s);
        } catch (Exception e) {
            return null;
        }
    }
}
