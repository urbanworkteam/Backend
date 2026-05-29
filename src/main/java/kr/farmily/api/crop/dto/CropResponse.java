package kr.farmily.api.crop.dto;

import kr.farmily.api.crop.domain.Crop;

public record CropResponse(Long id, String name, String colorHex, String stage) {

    public static CropResponse from(Crop c) {
        return new CropResponse(c.getId(), c.getName(), c.getColorHex(), c.getStage());
    }
}
