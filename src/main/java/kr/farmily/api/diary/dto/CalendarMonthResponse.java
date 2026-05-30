package kr.farmily.api.diary.dto;

import kr.farmily.api.diary.domain.WorkType;

import java.time.LocalDate;
import java.util.List;

public record CalendarMonthResponse(List<Day> days) {

    public record Day(LocalDate date, List<Tag> tags, List<Long> diaryIds) {}

    public record Tag(String crop, String color, WorkType workType) {}
}
