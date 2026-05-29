package kr.farmily.api.diary.domain;

import java.util.List;

public enum WorkType {
    TILLAGE("경운", "🌱"),
    IRRIGATION("관수", "💧"),
    SEEDING("파종·모내기", "🌾"),
    WEEDING("제초", "✂️"),
    HARVEST("수확", "🧺"),
    OTHER_FARMING("기타 농업활동", "🚜"),
    DAILY("하루 일상", "🙂");

    public final String label;
    public final String icon;

    WorkType(String label, String icon) {
        this.label = label;
        this.icon = icon;
    }

    public static List<WorkType> all() {
        return List.of(values());
    }
}
