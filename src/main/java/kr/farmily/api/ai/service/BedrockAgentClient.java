package kr.farmily.api.ai.service;

import kr.farmily.api.ai.config.AiProperties;
import kr.farmily.api.ai.domain.ContentJob;
import kr.farmily.api.ai.domain.Platform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeAsyncClient;

import java.time.Duration;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class BedrockAgentClient {

    private final BedrockAgentRuntimeAsyncClient bedrock;
    private final AiProperties props;
    private final WebClient agentCoreWebClient;

    public Result invoke(ContentJob job, String diarySummary) {
        if ("mock".equalsIgnoreCase(props.provider())) {
            return mock(job);
        }
        return invokeAgentCore(job);
    }

    @SuppressWarnings("unchecked")
    private Result invokeAgentCore(ContentJob job) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("userId", String.valueOf(job.getUserId()));
        body.put("platform", job.getPlatform() == null ? Platform.INSTAGRAM.name() : job.getPlatform().name());
        body.put("diaryIds", job.getDiaryIds() == null ? List.of() : Arrays.asList(job.getDiaryIds()));
        body.put("keywords", job.getKeywords() != null ? job.getKeywords() : "");
        body.put("photoS3Keys", job.getExtraPhotoKeys() == null ? List.of() : Arrays.asList(job.getExtraPhotoKeys()));
        body.put("jobId", job.getId());

        log.info("AgentCore 호출 시작: userId={}, platform={}, diaryIds={}",
                body.get("userId"), body.get("platform"), body.get("diaryIds"));

        Map<?, ?> response = agentCoreWebClient.post()
                .uri("/invocations")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(props.invokeTimeoutSeconds()))
                .block();

        if (response == null || response.containsKey("error")) {
            String error = response != null ? String.valueOf(response.get("error")) : "null response";
            throw new RuntimeException("AgentCore 호출 실패: " + error);
        }

        log.info("AgentCore 응답 수신: agentCoreJobId={}, status={}", response.get("jobId"), response.get("status"));

        String caption = (String) response.get("caption");
        List<String> hashtags = (List<String>) response.get("hashtags");
        List<String> cardImageKeysList = (List<String>) response.get("cardImageKeys");

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("provider", "agentcore");
        meta.put("platform", body.get("platform"));
        meta.put("agentCoreJobId", response.get("jobId"));
        meta.put("angle", response.get("angle"));
        meta.put("contentType", response.get("contentType"));
        meta.put("textPool", response.get("textPool"));

        String[] cardImageKeys = (cardImageKeysList != null && !cardImageKeysList.isEmpty())
                ? cardImageKeysList.toArray(String[]::new)
                : new String[]{};

        return new Result(
                cardImageKeys,
                caption,
                hashtags == null ? new String[0] : hashtags.toArray(String[]::new),
                meta
        );
    }

    private Result mock(ContentJob job) {
        if (job.getPlatform() == Platform.SMARTSTORE) {
            Map<String, Object> productInfo = new LinkedHashMap<>();
            productInfo.put("품종", "설향");
            productInfo.put("중량", "500g");
            productInfo.put("원산지", "국내산 (충남 논산)");
            productInfo.put("재배방식", "친환경 무농약");
            productInfo.put("발송", "주문일 기준 익일 발송");

            Map<String, Object> storeMeta = new LinkedHashMap<>();
            storeMeta.put("brix", "18°Bx");
            storeMeta.put("harvestPolicy", "당일 수확 발송");
            storeMeta.put("farmingYears", "20년 재배 경력");
            storeMeta.put("reasonsToBuy", List.of(
                    "20년 경력 농부가 직접 재배",
                    "당일 수확 → 신선도 보장",
                    "친환경 무농약 인증"));
            storeMeta.put("productInfo", productInfo);
            storeMeta.put("price", 24900);

            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("provider", "mock");
            meta.put("platform", "SMARTSTORE");
            meta.put("storeMeta", storeMeta);

            return new Result(
                    new String[]{"ai-results/mock/" + UUID.randomUUID() + ".jpg"},
                    null,
                    null,
                    meta
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
