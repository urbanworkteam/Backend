package kr.farmily.api.diary.service;

import kr.farmily.api.crop.domain.Crop;
import kr.farmily.api.crop.repository.CropRepository;
import kr.farmily.api.diary.domain.FarmDiary;
import kr.farmily.api.diary.dto.CalendarMonthResponse;
import kr.farmily.api.diary.repository.FarmDiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryCalendarService {

    private final FarmDiaryRepository diaryRepository;
    private final CropRepository cropRepository;

    @Transactional(readOnly = true)
    public CalendarMonthResponse getMonth(long userId, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate first = ym.atDay(1);
        LocalDate last = ym.atEndOfMonth();
        List<FarmDiary> diaries = diaryRepository.findActiveInRange(userId, first, last);

        Set<Long> cropIds = diaries.stream().map(FarmDiary::getCropId).collect(Collectors.toSet());
        Map<Long, Crop> crops = cropRepository.findAllById(cropIds).stream()
                .collect(Collectors.toMap(Crop::getId, c -> c));

        Map<LocalDate, List<CalendarMonthResponse.Tag>> byDate = new HashMap<>();
        Map<LocalDate, List<Long>> idsByDate = new HashMap<>();
        for (FarmDiary d : diaries) {
            Crop c = crops.get(d.getCropId());
            String cropName = c != null ? c.getName() : "";
            String color = c != null ? c.getColorHex() : "#A3A3A3";
            d.getWorkBlocks().forEach(wb -> byDate
                    .computeIfAbsent(d.getDiaryDate(), k -> new ArrayList<>())
                    .add(new CalendarMonthResponse.Tag(cropName, color, wb.getWorkType())));
            idsByDate.computeIfAbsent(d.getDiaryDate(), k -> new ArrayList<>()).add(d.getId());
        }

        List<CalendarMonthResponse.Day> days = new ArrayList<>();
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {
            LocalDate date = ym.atDay(d);
            days.add(new CalendarMonthResponse.Day(
                    date,
                    byDate.getOrDefault(date, List.of()),
                    idsByDate.getOrDefault(date, List.of())));
        }
        return new CalendarMonthResponse(days);
    }
}
