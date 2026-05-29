package kr.farmily.api.diary.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import kr.farmily.api.diary.domain.WorkType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record WriteDiaryRequest(
        @NotNull LocalDate date,
        @NotNull Long farmLocationId,
        @NotNull Long cropId,
        @NotNull @Valid WeatherInput weather,
        @NotNull @Size(min = 1, max = 10) @Valid List<WorkBlockInput> workBlocks,
        @Size(max = 2000) String memo,
        @Size(max = 5) List<@NotBlank String> photoKeys
) {

    public record WeatherInput(
            @NotBlank @Pattern(regexp = "AUTO|MANUAL") String source,
            String main,
            BigDecimal tempMax,
            BigDecimal tempMin,
            BigDecimal precipitationMm,
            Integer humidityPct
    ) {}

    public record WorkBlockInput(
            @NotNull WorkType workType,
            @Size(max = 500) String detail
    ) {}
}
