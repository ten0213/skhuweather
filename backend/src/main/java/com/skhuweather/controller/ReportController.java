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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private static final int REPORT_WINDOW_HOURS = 3;
    private static final int MAX_REPORTS_PER_CLIENT_IN_WINDOW = 2;
    private static final int MAX_REPORTS_PER_IP_IN_WINDOW = 20;
    private static final int MIN_REPORT_INTERVAL_SECONDS = 3;

    // 동일 클라이언트의 동시 중복 요청 차단용 잠금 집합
    private final Set<String> processingClients = ConcurrentHashMap.newKeySet();

    @Autowired
    private WeatherReportRepository reportRepo;

    // 현재 3시간 기준 각 날씨 유형별 제보 수 반환
    @GetMapping
    public Map<String, Long> getReports() {
        LocalDateTime threeHoursAgo = LocalDateTime.now().minusHours(REPORT_WINDOW_HOURS);
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("rainy",  reportRepo.countByWeatherTypeAndCreatedAtAfter(0, threeHoursAgo));
        counts.put("cloudy", reportRepo.countByWeatherTypeAndCreatedAtAfter(1, threeHoursAgo));
        counts.put("sunny",  reportRepo.countByWeatherTypeAndCreatedAtAfter(2, threeHoursAgo));
        counts.put("dusty",  reportRepo.countByWeatherTypeAndCreatedAtAfter(3, threeHoursAgo));
        counts.put("windy",  reportRepo.countByWeatherTypeAndCreatedAtAfter(4, threeHoursAgo));
        counts.put("snowy",  reportRepo.countByWeatherTypeAndCreatedAtAfter(5, threeHoursAgo));
        return counts;
    }

    // 날씨 제보 제출 (3시간 내 동일 클라이언트 중복 방지 + IP 단위 과다 제보 제한)
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
        String acceptLanguage = request.getHeader("Accept-Language");
        String clientFingerprint = buildClientFingerprint(ipAddress, userAgent, acceptLanguage);

        // 동시 중복 요청 차단: 이미 처리 중인 동일 클라이언트 요청이면 즉시 거부
        if (!processingClients.add(clientFingerprint)) {
            return ResponseEntity.status(429)
                    .body(Map.of("message", "처리 중입니다."));
        }

        try {
            LocalDateTime threeHoursAgo = LocalDateTime.now().minusHours(REPORT_WINDOW_HOURS);

            // 최소 제보 간격 체크: 3초 이내 연속 제보 차단
            LocalDateTime intervalAgo = LocalDateTime.now().minusSeconds(MIN_REPORT_INTERVAL_SECONDS);
            if (reportRepo.countBySessionIdAndCreatedAtAfter(clientFingerprint, intervalAgo) > 0) {
                return ResponseEntity.status(429)
                        .body(Map.of("message", "처리 중입니다."));
            }

            // 동일 클라이언트(IP/브라우저 지문)는 3시간 내 2회만 허용
            long clientReportCount = reportRepo.countBySessionIdAndCreatedAtAfter(clientFingerprint, threeHoursAgo);
            if (clientReportCount >= MAX_REPORTS_PER_CLIENT_IN_WINDOW) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "제보는 3시간에 2번만 가능합니다."));
            }

            // NAT/공용망 환경을 고려해 IP는 완전 차단이 아닌 3시간 누적 상한으로 제한
            long ipReportCount = reportRepo.countByIpAddressAndCreatedAtAfter(ipAddress, threeHoursAgo);
            if (ipReportCount >= MAX_REPORTS_PER_IP_IN_WINDOW) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "동일 네트워크에서 제보가 많습니다. 잠시 후 다시 시도해주세요."));
            }

            // sessionId 컬럼은 서버가 계산한 클라이언트 지문 저장용으로 사용
            WeatherReport report = new WeatherReport(weatherType, clientFingerprint, ipAddress, LocalDateTime.now());
            reportRepo.save(report);

            return ResponseEntity.ok(Map.of("message", "제보 완료!"));

        } finally {
            processingClients.remove(clientFingerprint);
        }
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
        String remoteAddr = normalizeIp(request.getRemoteAddr());
        if (remoteAddr == null) {
            return "unknown";
        }

        // 신뢰 가능한 프록시를 거친 요청에서만 전달 헤더를 사용한다.
        if (!isTrustedProxyIp(remoteAddr)) {
            return remoteAddr;
        }

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

        return remoteAddr;
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

    private boolean isTrustedProxyIp(String ip) {
        if (ip == null) {
            return false;
        }

        if ("::1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || ip.startsWith("127.")) {
            return true;
        }

        if (ip.startsWith("10.") || ip.startsWith("192.168.")) {
            return true;
        }

        if (ip.startsWith("172.")) {
            String[] parts = ip.split("\\.");
            if (parts.length >= 2) {
                try {
                    int secondOctet = Integer.parseInt(parts[1]);
                    if (secondOctet >= 16 && secondOctet <= 31) {
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                    return false;
                }
            }
        }

        String lower = ip.toLowerCase();
        return lower.startsWith("fc") || lower.startsWith("fd") || lower.startsWith("fe80:");
    }

    private String normalizeHeader(String value) {
        if (value == null) {
            return "unknown";
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? "unknown" : normalized;
    }

    private String buildClientFingerprint(String ipAddress, String userAgent, String acceptLanguage) {
        String normalizedIp = ipAddress == null ? "unknown" : ipAddress;
        String normalizedUa = normalizeHeader(userAgent);
        String normalizedLang = normalizeHeader(acceptLanguage);
        String payload = "ipua|" + normalizedIp + "|" + normalizedUa + "|" + normalizedLang;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }
}
