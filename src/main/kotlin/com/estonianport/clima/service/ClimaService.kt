package com.estonianport.clima.service

import com.estonianport.clima.repository.ClimaRepository
import com.estonianport.clima.dto.ClimaResponse
import com.estonianport.clima.model.Clima
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

private val logger = KotlinLogging.logger {}

@Service
class ClimaService(
    private val climaRepository: ClimaRepository,
    private val openWeatherService: OpenWeatherService
) {

    @Transactional
    fun fetchAndSaveWeather() {
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)

        // Verificar si ya existe un registro para esta hora
        if (climaRepository.existsByTimestamp(now)) {
            logger.info { "Ya existe un registro para la hora: $now. Omitiendo..." }
            return
        }

        val weatherData = openWeatherService.getCurrentWeather()

        if (weatherData == null) {
            logger.warn { "No se pudo obtener datos del clima. No se guardará registro." }
            return
        }

        val isRaining = weatherData.weather.any {
            it.main.equals("Rain", ignoreCase = true) ||
                    it.main.equals("Drizzle", ignoreCase = true) ||
                    it.main.equals("Thunderstorm", ignoreCase = true)
        }

        val weatherHourly = Clima(
            timestamp = now,
            temperatura = BigDecimal.valueOf(weatherData.main.temp),
            humedad = weatherData.main.humidity,
            estaLloviendo = isRaining
        )

        climaRepository.save(weatherHourly)
        logger.info { "Registro de clima guardado: temp=${weatherData.main.temp}°C, humedad=${weatherData.main.humidity}%, lluvia=$isRaining" }
    }

    fun getLatestWeather(): ClimaResponse? {
        return climaRepository.findTopByOrderByTimestampDesc()?.toDto()
    }

    fun getWeatherHistory(from: LocalDateTime, to: LocalDateTime): List<ClimaResponse> {
        return climaRepository.findByTimestampBetweenOrderByTimestampAsc(from, to)
            .map { it.toDto() }
    }

    private fun Clima.toDto() = ClimaResponse(
        timestamp = timestamp,
        temperature = temperatura,
        humidity = humedad,
        isRaining = estaLloviendo
    )
}