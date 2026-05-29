package kr.farmily.api.diary.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "diary_photos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiaryPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false)
    private FarmDiary diary;

    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    @Column(name = "size_bytes", nullable = false)
    private Integer sizeBytes;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public static DiaryPhoto of(FarmDiary diary, String s3Key, int sortOrder) {
        DiaryPhoto p = new DiaryPhoto();
        p.diary = diary;
        p.s3Key = s3Key;
        p.sizeBytes = 0;
        p.sortOrder = sortOrder;
        return p;
    }
}
