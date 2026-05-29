package kr.farmily.api.farmlocation.dto;

import kr.farmily.api.farmlocation.domain.FarmLocation;

import java.math.BigDecimal;

public record FarmLocationResponse(
        Long id, String label, String address,
        BigDecimal lat, BigDecimal lng,
        Integer kmaGridX, Integer kmaGridY,
        Integer sortOrder
) {

    public static FarmLocationResponse from(FarmLocation l) {
        return new FarmLocationResponse(
                l.getId(), l.getLabel(), l.getAddress(),
                l.getLat(), l.getLng(),
                l.getKmaGridX(), l.getKmaGridY(),
                l.getSortOrder()
        );
    }
}
