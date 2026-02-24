package com.skhuweather.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class WeatherController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${external.weather.url}")
    private String externalWeatherUrl;

    @GetMapping("/weather")
    public Object getWeather() {
        return restTemplate.getForObject(externalWeatherUrl + "/getWeather", Object.class);
    }
}
