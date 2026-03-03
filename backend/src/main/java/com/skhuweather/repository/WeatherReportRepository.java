package com.skhuweather.repository;

import com.skhuweather.entity.WeatherReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface WeatherReportRepository extends JpaRepository<WeatherReport, Long> {

    long countByWeatherTypeAndCreatedAtAfter(int weatherType, LocalDateTime since);

<<<<<<< HEAD
    boolean existsBySessionIdAndCreatedAtAfter(String sessionId, LocalDateTime since);

    boolean existsByIpAddressAndCreatedAtAfter(String ipAddress, LocalDateTime since);
=======
    long countBySessionIdAndCreatedAtAfter(String sessionId, LocalDateTime since);

    long countByIpAddressAndCreatedAtAfter(String ipAddress, LocalDateTime since);
>>>>>>> 8a9cd08a9d3b6ccb6869711738dc61ea117d9c03
}
