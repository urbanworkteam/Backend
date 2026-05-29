package kr.farmily.api.crop.service;

import java.util.List;

public final class CropColorPalette {

    private CropColorPalette() {}

    private static final List<String> PALETTE = List.of(
            "#FF5A5A", "#FFA94D", "#FFD43B", "#69DB7C", "#3BC9DB",
            "#4DABF7", "#9775FA", "#F783AC", "#A3A3A3", "#2BA651",
            "#FF8787", "#E599F7"
    );

    public static String pickByIndex(long index) {
        int i = (int) Math.floorMod(index, PALETTE.size());
        return PALETTE.get(i);
    }
}
