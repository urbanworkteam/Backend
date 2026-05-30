package kr.farmily.api.ai.service;

import kr.farmily.api.ai.config.AiProperties;
import kr.farmily.api.ai.domain.ContentJob;
import kr.farmily.api.ai.domain.Platform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeAsyncClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BedrockAgentClient {

    private final BedrockAgentRuntimeAsyncClient bedrock;
    private final AiProperties props;

    public Result invoke(ContentJob job, String diarySummary) {
        // TODO: Action Group 응답 파싱 — 실제 Bedrock 응답 스트림 처리는 INFRA-002/003 완료 후 구현.
        // 현재는 provider 값과 무관하게 mock 결과 반환 (SDK 비동기 스트림 핸들러 구현 전).
        if (!"mock".equalsIgnoreCase(props.provider())) {
            log.debug("Bedrock provider={} 이지만 응답 스트림 핸들러 미구현 → mock 반환 (bedrock client present={}, summaryLen={})",
                    props.provider(), bedrock != null, diarySummary == null ? 0 : diarySummary.length());
        }
        return mock(job);
    }

    private Result mock(ContentJob job) {
        if (job.getPlatform() == Platform.SMARTSTORE) {
            return new Result(
                    new String[]{"ai-results/mock/" + UUID.randomUUID() + ".jpg"},
                    null,
                    null,
                    Map.of("provider", "mock", "platform", "SMARTSTORE")
            );
        }
        return new Result(
                new String[]{
                        "ai-results/mock/" + UUID.randomUUID() + ".jpg",
                        "ai-results/mock/" + UUID.randomUUID() + ".jpg",
                        "ai-results/mock/" + UUID.randomUUID() + ".jpg",
                        "ai-results/mock/" + UUID.randomUUID() + ".jpg",
                        "ai-results/mock/" + UUID.randomUUID() + ".jpg"
                },
                "오늘 첫 수확 🎉 정성껏 키운 작물을 만나보세요.",
                List.of("#farmily", "#친환경", "#수확", "#로컬푸드", "#농부의일상").toArray(String[]::new),
                Map.of("provider", "mock", "platform", "INSTAGRAM")
        );
    }

    public record Result(String[] cardImageKeys, String caption, String[] hashtags, Map<String, Object> meta) {}
}
