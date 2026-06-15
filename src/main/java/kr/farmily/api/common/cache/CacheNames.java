package kr.farmily.api.common.cache;

/**
 * Redis 캐시 이름 상수. TTL 은 {@link kr.farmily.api.common.config.RedisConfig} 에서 캐시별로 지정.
 */
public final class CacheNames {

    private CacheNames() {}

    /**
     * 캐시 키 prefix 버전. 직렬화 포맷이 바뀌면 이 값을 bump(v2→v3)해서 옛 키와 자동 격리한다.
     * (포맷 불일치로 인한 역직렬화 SerializationException 재발 방지. {@link kr.farmily.api.common.config.RedisConfig} 의 prefix 에서 사용)
     */
    public static final String VERSION = "v2";

    /** 기상청 단기예보 (grid:date 키, 60분) */
    public static final String WEATHER = "weather";
    /** 공개 농장 프로필 (handle 키, 10분) */
    public static final String PUBLIC_PROFILE = "publicProfile";
    /** 내 프로필 (userId 키, 10분) */
    public static final String MY_PROFILE = "myProfile";
    /** 마이페이지 대시보드 (userId 키, 5분) */
    public static final String MY_PAGE = "myPage";
    /** 내 작물 목록 (userId 키, 15분) */
    public static final String CROPS = "crops";
    /** 알림 설정 (userId 키, 30분) */
    public static final String NOTIFICATION_SETTINGS = "notificationSettings";
    /** 영농일지 월 캘린더 (userId:year:month 키, 30분) */
    public static final String DIARY_CALENDAR = "diaryCalendar";
    /** 작업 유형 정적 목록 (24시간) */
    public static final String WORK_TYPES = "workTypes";
}
