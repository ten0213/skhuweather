package com.skhuweather.controller;

import com.skhuweather.entity.WeatherReport;
import com.skhuweather.repository.WeatherReportRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
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

    // 날씨 제보 제출 (3시간 내 동일 클라이언트 지문/IP 중복 방지)
    @PostMapping
    public ResponseEntity<Map<String, String>> submitReport(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {

        Integer weatherType = parseWeatherType(body.get("weatherType"));
        if (weatherType == null || weatherType < 0 || weatherType > 5) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "유효하지 않은 날씨 제보 유형입니다."));
        }

        String ipAddress = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String clientFingerprint = buildClientFingerprint(ipAddress, userAgent);

        LocalDateTime threeHoursAgo = LocalDateTime.now().minusHours(3);

        // 쿠키 삭제로 우회되지 않도록 서버 계산 지문 + IP를 동시에 확인
        boolean duplicated = reportRepo.existsBySessionIdAndCreatedAtAfter(clientFingerprint, threeHoursAgo)
                || reportRepo.existsByIpAddressAndCreatedAtAfter(ipAddress, threeHoursAgo);

        if (duplicated) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "제보는 3시간에 1번만 가능합니다."));
        }

        // sessionId 컬럼은 서버가 계산한 클라이언트 지문 저장용으로 사용
        WeatherReport report = new WeatherReport(weatherType, clientFingerprint, ipAddress, LocalDateTime.now());
        reportRepo.save(report);

        return ResponseEntity.ok(Map.of("message", "제보 완료!"));
    }

    private Integer parseWeatherType(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }

        if (value instanceof String str) {
            try {
                return Integer.parseInt(str.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    private String extractClientIp(HttpServletRequest request) {
        String xRealIp = normalizeIp(request.getHeader("X-Real-IP"));
        if (xRealIp != null) {
            return xRealIp;
        }

        String cfConnectingIp = normalizeIp(request.getHeader("CF-Connecting-IP"));
        if (cfConnectingIp != null) {
            return cfConnectingIp;
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String[] candidates = forwardedFor.split(",");

            // 일반적으로 X-Forwarded-For의 가장 왼쪽 값이 원본 클라이언트 IP다.
            for (String rawCandidate : candidates) {
                String candidate = normalizeIp(rawCandidate);
                if (candidate != null) {
                    return candidate;
                }
            }
        }

        String remoteAddr = normalizeIp(request.getRemoteAddr());
        return remoteAddr != null ? remoteAddr : "unknown";
    }

    private String normalizeIp(String rawValue) {
        if (rawValue == null) {
            return null;
        }

        String value = rawValue.trim();
        if (value.isBlank() || "unknown".equalsIgnoreCase(value)) {
            return null;
        }

        // [IPv6]:port 형식 처리
        if (value.startsWith("[") && value.contains("]")) {
            value = value.substring(1, value.indexOf(']'));
        }

        // IPv4:port 형식 처리
        if (value.contains(".") && value.chars().filter(ch -> ch == ':').count() == 1) {
            value = value.substring(0, value.indexOf(':'));
        }

        try {
            InetAddress.getByName(value);
            return value;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String buildClientFingerprint(String ipAddress, String userAgent) {
        String normalizedIp = ipAddress == null ? "unknown" : ipAddress;
        String normalizedUa = (userAgent == null || userAgent.isBlank()) ? "unknown" : userAgent.trim();
        String payload = normalizedIp + "|" + normalizedUa;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }
}
