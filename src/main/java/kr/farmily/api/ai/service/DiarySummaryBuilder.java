package kr.farmily.api.ai.service;

import kr.farmily.api.diary.domain.FarmDiary;
import kr.farmily.api.diary.service.DiaryReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DiarySummaryBuilder {

    private final DiaryReadService diaryReadService;

    public String build(long userId, Long[] diaryIds) {
        if (diaryIds == null || diaryIds.length == 0) return "(없음)";
        List<FarmDiary> diaries = diaryReadService.findManyByIds(userId, List.of(diaryIds));
        return diaries.stream().map(d -> {
            String works = d.getWorkBlocks().stream()
                    .map(b -> b.getWorkType().name() + (b.getDetail() != null ? "(" + b.getDetail() + ")" : ""))
                    .collect(Collectors.joining(", "));
            return "- " + d.getDiaryDate() + " | 작업: " + works
                    + (d.getMemo() != null ? " | 메모: " + d.getMemo() : "");
        }).collect(Collectors.joining("\n"));
    }
}
