package kr.farmily.api.ai.service;

import kr.farmily.api.ai.config.AiProperties;
import kr.farmily.api.ai.domain.ContentJob;
import kr.farmily.api.ai.domain.Platform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeAsyncClient;

import java.util.LinkedHashMap;
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
