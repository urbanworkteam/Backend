package kr.farmily.api.diary.service;

import kr.farmily.api.common.cache.CacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 일지 작성/수정/삭제 시 해당 날짜가 속한 월 캘린더 캐시를 무효화.
 * 키 포맷은 {@code DiaryCalendarService#getMonth} 의 @Cacheable 키와 동일: {@code userId:year:month}.
 */
@Component
@RequiredArgsConstructor
public class DiaryCalendarCacheEvictor {

    private final CacheManager cacheManager;

    public void evict(long userId, LocalDate date) {
        if (date == null) return;
        Cache cache = cacheManager.getCache(CacheNames.DIARY_CALENDAR);
        if (cache != null) {
            cache.evict(userId + ":" + date.getYear() + ":" + date.getMonthValue());
        }
    }
}
