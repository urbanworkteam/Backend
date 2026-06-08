package kr.farmily.api.diary.dto;

import kr.farmily.api.crop.domain.Crop;
import kr.farmily.api.diary.domain.DiaryWorkBlock;
import kr.farmily.api.diary.domain.FarmDiary;
import kr.farmily.api.diary.domain.WorkType;
import kr.farmily.api.farmlocation.domain.FarmLocation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Function;

public record DiaryResponse(
        Long id,
        LocalDate date,
        FarmLocationSummary farmLocation,
        CropSummary crop,
        WeatherDto weather,
        List<WorkBlockDto> workBlocks,
        String memo,
        List<PhotoDto> photos,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public record FarmLocationSummary(Long id, String label) {}

    public record CropSummary(Long id, String name, String colorHex) {}

    public record WeatherDto(String main, BigDecimal tempMax, BigDecimal tempMin,
                             BigDecimal precipitationMm, Integer humidityPct, String source) {}

    public record WorkBlockDto(Long id, WorkType workType, String detail, Integer sortOrder) {}

    public record PhotoDto(Long id, String url, String s3Key, Integer sortOrder) {}

    public static DiaryResponse from(FarmDiary d, FarmLocation loc, Crop crop, Function<String, String> toUrl) {
        WeatherDto w = new WeatherDto(
                d.getWeather() == null ? null : d.getWeather().getMain(),
                d.getWeather() == null ? null : d.getWeather().getTempMax(),
                d.getWeather() == null ? null : d.getWeather().getTempMin(),
                d.getWeather() == null ? null : d.getWeather().getPrecipitationMm(),
                d.getWeather() == null ? null : d.getWeather().getHumidityPct(),
                d.getWeather() == null ? null : d.getWeather().getSource()
        );
        List<WorkBlockDto> blocks = d.getWorkBlocks().stream()
                .map(b -> new WorkBlockDto(b.getId(), b.getWorkType(), b.getDetail(), b.getSortOrder()))
                .toList();
        List<PhotoDto> photos = d.getPhotos().stream()
                .map(p -> new PhotoDto(p.getId(), toUrl.apply(p.getS3Key()), p.getS3Key(), p.getSortOrder()))
                .toList();
        return new DiaryResponse(
                d.getId(),
                d.getDiaryDate(),
                loc == null ? null : new FarmLocationSummary(loc.getId(), loc.getLabel()),
                crop == null ? null : new CropSummary(crop.getId(), crop.getName(), crop.getColorHex()),
                w,
                blocks,
                d.getMemo(),
                photos,
                d.getCreatedAt(),
                d.getUpdatedAt()
        );
    }
}
