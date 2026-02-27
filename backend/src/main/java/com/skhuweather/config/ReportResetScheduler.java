package com.skhuweather.config;

import com.skhuweather.repository.WeatherReportRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReportResetScheduler {

    @Autowired
    private WeatherReportRepository reportRepo;

    @PostConstruct
    public void resetOnStartup() {
        reportRepo.deleteAll();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void resetAtMidnight() {
        reportRepo.deleteAll();
    }
}
