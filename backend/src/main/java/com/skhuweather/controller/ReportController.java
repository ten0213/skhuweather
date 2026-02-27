package com.skhuweather.controller;

import com.skhuweather.entity.WeatherReport;
import com.skhuweather.repository.WeatherReportRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

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

    // 날씨 제보 제출 (3시간 내 동일 기기 중복 방지 - IP + HttpOnly 쿠키 기반)
    @PostMapping
    public ResponseEntity<Map<String, String>> submitReport(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request,
            HttpServletResponse response) {

        int weatherType = (int) body.get("weatherType");

        // 클라이언트 IP 추출 (프록시/로드밸런서 고려)
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isBlank()) {
            ipAddress = request.getRemoteAddr();
        } else {
            // X-Forwarded-For는 "client, proxy1, proxy2" 형태일 수 있음
            ipAddress = ipAddress.split(",")[0].trim();
        }

        // HttpOnly 쿠키에서 식별자 추출 (없으면 새로 생성)
        String reporterId = null;
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("skhu_reporter_id".equals(c.getName())) {
                    reporterId = c.getValue();
                    break;
                }
            }
        }
        if (reporterId == null || reporterId.isBlank()) {
            reporterId = UUID.randomUUID().toString();
        }

        // 쿠키 갱신 (HttpOnly로 JS 접근 불가)
        Cookie cookie = new Cookie("skhu_reporter_id", reporterId);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(3 * 60 * 60); // 3시간
        response.addCookie(cookie);

        LocalDateTime threeHoursAgo = LocalDateTime.now().minusHours(3);

        // IP 기반 중복 검사 (쿠키 삭제 우회 방지)
        if (reportRepo.existsByIpAddressAndCreatedAtAfter(ipAddress, threeHoursAgo)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "제보는 3시간에 1번만 가능합니다."));
        }

        // 쿠키 기반 중복 검사 (추가 보호)
        if (reportRepo.existsBySessionIdAndCreatedAtAfter(reporterId, threeHoursAgo)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "제보는 3시간에 1번만 가능합니다."));
        }

        WeatherReport report = new WeatherReport(weatherType, reporterId, ipAddress, LocalDateTime.now());
        reportRepo.save(report);

        return ResponseEntity.ok(Map.of("message", "제보 완료!"));
    }
}
