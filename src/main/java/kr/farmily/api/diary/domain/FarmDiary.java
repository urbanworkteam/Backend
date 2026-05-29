package kr.farmily.api.diary.domain;

import jakarta.persistence.*;
import kr.farmily.api.common.entity.BaseTimeEntity;
import kr.farmily.api.weather.domain.WeatherSnapshot;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "farm_diaries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FarmDiary extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "farm_location_id")
    private Long farmLocationId;

    @Column(name = "diary_date", nullable = false)
    private LocalDate diaryDate;

    @Column(name = "crop_id", nullable = false)
    private Long cropId;

    @Embedded
    private WeatherSnapshot weather;

    private String memo;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<DiaryWorkBlock> workBlocks = new ArrayList<>();

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<DiaryPhoto> photos = new ArrayList<>();

    public static FarmDiary create(long userId, Long farmLocationId, Long cropId,
                                   LocalDate date, WeatherSnapshot weather, String memo) {
        FarmDiary d = new FarmDiary();
        d.userId = userId;
        d.farmLocationId = farmLocationId;
        d.cropId = cropId;
        d.diaryDate = date;
        d.weather = weather;
        d.memo = memo;
        return d;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete() {
        this.deletedAt = OffsetDateTime.now();
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    public void updateWeather(WeatherSnapshot snapshot) {
        this.weather = snapshot;
    }

    public void updateDate(LocalDate date) {
        this.diaryDate = date;
    }

    public void updateFarmLocation(Long farmLocationId) {
        this.farmLocationId = farmLocationId;
    }

    public void updateCrop(Long cropId) {
        this.cropId = cropId;
    }

    public void addWorkBlock(WorkType type, String detail) {
        workBlocks.add(DiaryWorkBlock.of(this, type, detail, workBlocks.size()));
    }

    public void addPhoto(String s3Key) {
        photos.add(DiaryPhoto.of(this, s3Key, photos.size()));
    }

    public void clearWorkBlocks() {
        workBlocks.clear();
    }

    public void clearPhotos() {
        photos.clear();
    }
}
