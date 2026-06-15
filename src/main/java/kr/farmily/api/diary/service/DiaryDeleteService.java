package kr.farmily.api.diary.service;

import kr.farmily.api.diary.domain.FarmDiary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiaryDeleteService {

    private final DiaryReadService readService;
    private final DiaryCalendarCacheEvictor calendarCacheEvictor;

    @Transactional
    public void delete(long userId, long id) {
        FarmDiary d = readService.requireOwner(userId, id);
        d.softDelete();
        calendarCacheEvictor.evict(userId, d.getDiaryDate());
    }
}
