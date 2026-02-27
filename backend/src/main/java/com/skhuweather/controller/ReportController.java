package com.skhuweather.controller;

import com.skhuweather.entity.WeatherReport;
import com.skhuweather.repository.WeatherReportRepository;
import jakarta.servlet.http.HttpServletRequest;
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

    // 날씨 제보 제출 (3시간 내 동일 IP 중복 방지)
    @PostMapping
    public ResponseEntity<Map<String, String>> submitReport(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {

        int weatherType = (int) body.get("weatherType");

        // 클라이언트 IP 추출 (프록시/로드밸런서 고려)
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isBlank()) {
            ipAddress = request.getRemoteAddr();
        } else {
            // X-Forwarded-For는 "client, proxy1, proxy2" 형태일 수 있음
            ipAddress = ipAddress.split(",")[0].trim();
        }

        LocalDateTime threeHoursAgo = LocalDateTime.now().minusHours(3);

        // IP 기반 중복 검사
        if (reportRepo.existsByIpAddressAndCreatedAtAfter(ipAddress, threeHoursAgo)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "제보는 3시간에 1번만 가능합니다."));
        }

        // 기존 스키마(sessionId NOT NULL) 호환을 위해 IP를 sessionId에도 저장
        WeatherReport report = new WeatherReport(weatherType, ipAddress, ipAddress, LocalDateTime.now());
        reportRepo.save(report);

        return ResponseEntity.ok(Map.of("message", "제보 완료!"));
    }
}
