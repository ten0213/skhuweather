package com.skhuweather.repository;

import com.skhuweather.entity.WeatherReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface WeatherReportRepository extends JpaRepository<WeatherReport, Long> {

    long countByWeatherTypeAndCreatedAtAfter(int weatherType, LocalDateTime since);

    boolean existsBySessionIdAndCreatedAtAfter(String sessionId, LocalDateTime since);
}
