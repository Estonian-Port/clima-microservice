package com.estonianport.clima.controller

import com.estonianport.clima.dto.ClimaResponse
import com.estonianport.clima.service.ClimaService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping
class ClimaController(
    private val climaService: ClimaService
) {

    @GetMapping("/weather/latest")
    fun getLatestWeather(): ResponseEntity<ClimaResponse> {
        val weather = climaService.getLatestWeather()
        return if (weather != null) {
            ResponseEntity.ok(weather)
        } else {
            ResponseEntity.noContent().build()
        }
    }

    @GetMapping("/weather")
    fun getWeatherHistory(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: LocalDateTime
    ): ResponseEntity<List<ClimaResponse>> {
        val history = climaService.getWeatherHistory(from, to)
        return ResponseEntity.ok(history)
    }

    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("status" to "UP"))
    }
}