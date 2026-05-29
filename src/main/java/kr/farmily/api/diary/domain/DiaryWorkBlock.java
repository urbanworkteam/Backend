package kr.farmily.api.diary.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "diary_work_blocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiaryWorkBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false)
    private FarmDiary diary;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_type", nullable = false)
    private WorkType workType;

    private String detail;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public static DiaryWorkBlock of(FarmDiary diary, WorkType type, String detail, int sortOrder) {
        DiaryWorkBlock b = new DiaryWorkBlock();
        b.diary = diary;
        b.workType = type;
        b.detail = detail;
        b.sortOrder = sortOrder;
        return b;
    }
}
