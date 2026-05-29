package kr.farmily.api.profile.service;

import kr.farmily.api.diary.dto.CalendarMonthResponse;
import kr.farmily.api.diary.dto.DiaryResponse;
import kr.farmily.api.diary.repository.FarmDiaryRepository;
import kr.farmily.api.diary.service.DiaryCalendarService;
import kr.farmily.api.diary.service.DiaryReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileCalendarService {

    private final DiaryCalendarService diaryCalendarService;
    private final DiaryReadService diaryReadService;
    private final FarmDiaryRepository diaryRepository;

    @Transactional(readOnly = true)
    public CalendarMonthResponse getMonth(long userId, int year, int month) {
        return diaryCalendarService.getMonth(userId, year, month);
    }

    @Transactional(readOnly = true)
    public List<DiaryResponse> getDay(long userId, LocalDate date) {
        return diaryRepository.findActiveInRange(userId, date, date).stream()
                .map(d -> diaryReadService.findById(userId, d.getId()))
                .toList();
    }
}
