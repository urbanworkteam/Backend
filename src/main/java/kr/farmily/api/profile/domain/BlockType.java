package kr.farmily.api.profile.domain;

import java.util.Set;

public enum BlockType {
    CROP_INTRO,
    STORY,
    CALENDAR,
    DIVIDER,
    TEXT;

    public static final Set<BlockType> SYSTEM = Set.of(CROP_INTRO, STORY, CALENDAR, DIVIDER);

    public boolean isSystem() {
        return SYSTEM.contains(this);
    }
}
