package com.skhuweather.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_report")
public class WeatherReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 0=비, 1=흐림, 2=화창, 3=공기나쁨, 4=바람, 5=눈
    @Column(nullable = false)
    private int weatherType;

    @Column(nullable = false, length = 100)
    private String sessionId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public WeatherReport() {}

    public WeatherReport(int weatherType, String sessionId, LocalDateTime createdAt) {
        this.weatherType = weatherType;
        this.sessionId = sessionId;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public int getWeatherType() { return weatherType; }
    public void setWeatherType(int weatherType) { this.weatherType = weatherType; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
