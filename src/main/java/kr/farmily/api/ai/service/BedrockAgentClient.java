package kr.farmily.api.ai.service;

import kr.farmily.api.ai.config.AiProperties;
import kr.farmily.api.ai.domain.ContentJob;
import kr.farmily.api.ai.domain.Platform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvokeAgentRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class BedrockAgentClient {

    private final BedrockAgentRuntimeAsyncClient bedrock;
    private final AiProperties props;

    public Result invoke(ContentJob job, String diarySummary) {
        if ("mock".equalsIgnoreCase(props.provider())) {
            return mock(job);
        }
        try {
            String input = """
                    플랫폼: %s
                    크롭 id: %d
                    키워드: %s
                    영농일지 요약:
                    %s
                    추가 사진: %d장
                    """.formatted(
                    job.getPlatform(), job.getCropId(),
                    job.getKeywords() != null ? job.getKeywords() : "",
                    diarySummary,
                    job.getExtraPhotoKeys() == null ? 0 : job.getExtraPhotoKeys().length);

            InvokeAgentRequest req = InvokeAgentRequest.builder()
                    .agentId(props.bedrockAgentId())
                    .agentAliasId(props.bedrockAgentAliasId())
                    .sessionId("job-" + job.getId())
                    .inputText(input)
                    .build();

            bedrock.invokeAgent(req, b -> b.subscriber(part -> {}))
                    .get(props.invokeTimeoutSeconds() != null ? props.invokeTimeoutSeconds() : 120, TimeUnit.SECONDS);

            // TODO: Action Group 응답 파싱 — 실제 Bedrock 응답 스트림 처리는 INFRA-002/003 완료 후 구현.
            return mock(job);
        } catch (Exception e) {
            log.warn("Bedrock invoke 실패, mock 반환: {}", e.getMessage());
            return mock(job);
        }
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
