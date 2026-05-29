package kr.farmily.api.farmlocation.domain;

import jakarta.persistence.*;
import kr.farmily.api.common.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "farm_locations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FarmLocation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private String address;

    private BigDecimal lat;
    private BigDecimal lng;

    @Column(name = "kma_grid_x")
    private Integer kmaGridX;

    @Column(name = "kma_grid_y")
    private Integer kmaGridY;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    public static FarmLocation create(Long userId, String label, String address, int sortOrder) {
        FarmLocation l = new FarmLocation();
        l.userId = userId;
        l.label = label;
        l.address = address;
        l.sortOrder = sortOrder;
        return l;
    }

    public void update(String label, String address) {
        if (label != null) this.label = label;
        if (address != null) this.address = address;
    }

    public void applyGeocode(BigDecimal lat, BigDecimal lng, Integer gridX, Integer gridY) {
        this.lat = lat;
        this.lng = lng;
        this.kmaGridX = gridX;
        this.kmaGridY = gridY;
    }

    public boolean hasGrid() {
        return kmaGridX != null && kmaGridY != null;
    }
}
