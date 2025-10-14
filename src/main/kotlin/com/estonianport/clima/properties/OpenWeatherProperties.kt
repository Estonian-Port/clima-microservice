package com.estonianport.clima.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "openweather")
class OpenWeatherConfig {
    lateinit var apiKey: String
}