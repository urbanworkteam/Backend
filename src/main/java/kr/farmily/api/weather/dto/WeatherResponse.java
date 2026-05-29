package kr.farmily.api.weather.dto;

import kr.farmily.api.weather.domain.WeatherSnapshot;

import java.math.BigDecimal;

public record WeatherResponse(
        String main,
        BigDecimal tempMax,
        BigDecimal tempMin,
        BigDecimal precipitationMm,
        Integer humidityPct,
        String source
) {

    public static WeatherResponse from(WeatherSnapshot s) {
        return new WeatherResponse(
                s.getMain(), s.getTempMax(), s.getTempMin(),
                s.getPrecipitationMm(), s.getHumidityPct(), s.getSource()
        );
    }
}
