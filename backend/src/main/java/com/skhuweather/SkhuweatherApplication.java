package com.skhuweather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SkhuweatherApplication {
    public static void main(String[] args) {
        SpringApplication.run(SkhuweatherApplication.class, args);
    }
}
