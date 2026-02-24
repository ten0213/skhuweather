package com.skhuweather.controller;

import com.skhuweather.entity.WeatherReport;
import com.skhuweather.repository.WeatherReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private WeatherReportRepository reportRepo;

    // 현재 3시간 기준 각 날씨 유형별 제보 수 반환
    @GetMapping
    public Map<String, Long> getReports() {
        LocalDateTime threeHoursAgo = LocalDateTime.now().minusHours(3);
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("rainy",  reportRepo.countByWeatherTypeAndCreatedAtAfter(0, threeHoursAgo));
        counts.put("cloudy", reportRepo.countByWeatherTypeAndCreatedAtAfter(1, threeHoursAgo));
        counts.put("sunny",  reportRepo.countByWeatherTypeAndCreatedAtAfter(2, threeHoursAgo));
        counts.put("dusty",  reportRepo.countByWeatherTypeAndCreatedAtAfter(3, threeHoursAgo));
        counts.put("windy",  reportRepo.countByWeatherTypeAndCreatedAtAfter(4, threeHoursAgo));
        counts.put("snowy",  reportRepo.countByWeatherTypeAndCreatedAtAfter(5, threeHoursAgo));
        return counts;
    }

    // 날씨 제보 제출 (3시간 내 동일 세션 중복 방지)
    @PostMapping
    public ResponseEntity<Map<String, String>> submitReport(@RequestBody Map<String, Object> body) {
        String sessionId = (String) body.get("sessionId");
        int weatherType  = (int) body.get("weatherType");

        LocalDateTime threeHoursAgo = LocalDateTime.now().minusHours(3);

        if (reportRepo.existsBySessionIdAndCreatedAtAfter(sessionId, threeHoursAgo)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "제보는 3시간에 1번만 가능합니다."));
        }

        WeatherReport report = new WeatherReport(weatherType, sessionId, LocalDateTime.now());
        reportRepo.save(report);

        return ResponseEntity.ok(Map.of("message", "제보 완료!"));
    }
}
