package kr.farmily.api.crop.domain;

import jakarta.persistence.*;
import kr.farmily.api.common.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "crops")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Crop extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(name = "color_hex", nullable = false)
    private String colorHex;

    private String stage;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    public static Crop create(Long userId, String name, String colorHex, String stage) {
        Crop c = new Crop();
        c.userId = userId;
        c.name = name;
        c.colorHex = colorHex;
        c.stage = stage;
        return c;
    }

    public void update(String name, String colorHex, String stage) {
        if (name != null) this.name = name;
        if (colorHex != null) this.colorHex = colorHex;
        if (stage != null) this.stage = stage;
    }

    public void softDelete() {
        this.deletedAt = OffsetDateTime.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
