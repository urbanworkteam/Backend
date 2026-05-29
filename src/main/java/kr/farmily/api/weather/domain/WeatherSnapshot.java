package kr.farmily.api.weather.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class WeatherSnapshot {

    @Column(name = "weather_main")
    private String main;

    @Column(name = "temp_max")
    private BigDecimal tempMax;

    @Column(name = "temp_min")
    private BigDecimal tempMin;

    @Column(name = "precipitation_mm")
    private BigDecimal precipitationMm;

    @Column(name = "humidity_pct")
    private Integer humidityPct;

    @Column(name = "weather_source")
    private String source;

    public static WeatherSnapshot kma(String main, BigDecimal tempMax, BigDecimal tempMin,
                                      BigDecimal precipitationMm, Integer humidityPct) {
        return new WeatherSnapshot(main, tempMax, tempMin, precipitationMm, humidityPct, "KMA");
    }

    public static WeatherSnapshot manual(String main, BigDecimal tempMax, BigDecimal tempMin,
                                         BigDecimal precipitationMm, Integer humidityPct) {
        return new WeatherSnapshot(main, tempMax, tempMin, precipitationMm, humidityPct, "MANUAL");
    }

    public static WeatherSnapshot empty() {
        return new WeatherSnapshot(null, null, null, null, null, "MANUAL");
    }
}
