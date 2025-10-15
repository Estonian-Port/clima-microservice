package com.estonianport.clima.service

import com.estonianport.clima.repository.ClimaRepository
import com.estonianport.clima.dto.ClimaResponse
import com.estonianport.clima.dto.OpenWeatherResponse
import com.estonianport.clima.model.Clima
import com.estonianport.clima.model.enum.EstadoClimaType
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

        // Obtener el estado del clima basado en los datos de OpenWeather
        val estadoClima = determineEstadoClima(weatherData.weather)

        val nuevoClima = Clima(
            timestamp = now,
            temperatura = BigDecimal.valueOf(weatherData.main.temp),
            humedad = weatherData.main.humidity,
            estadoClima = estadoClima
        )

        climaRepository.save(nuevoClima)
        logger.info {
            "Registro de clima guardado: temp=${weatherData.main.temp}°C, " +
                    "humedad=${weatherData.main.humidity}%, estado=$estadoClima"
        }
    }

    fun getLatestWeather(): ClimaResponse? {
        return climaRepository.findTopByOrderByTimestampDesc()?.toDto()
    }

    fun getWeatherHistory(from: LocalDateTime, to: LocalDateTime): List<ClimaResponse> {
        return climaRepository.findByTimestampBetweenOrderByTimestampAsc(from, to)
            .map { it.toDto() }
    }

    /**
     * Determina el estado del clima basado en la lista de condiciones de OpenWeather
     * Prioriza: Tormenta > Lluvia > Nublado/Parcial > Soleado
     */
    private fun determineEstadoClima(weatherList: List<OpenWeatherResponse.Weather>): EstadoClimaType {
        // Si hay múltiples condiciones, priorizamos la más severa

        // 1. Verificar si hay tormenta (máxima prioridad)
        weatherList.forEach { weather ->
            if (weather.main.equals("Thunderstorm", ignoreCase = true)) {
                return EstadoClimaType.TORMENTA
            }
        }

        // 2. Verificar si hay lluvia
        weatherList.forEach { weather ->
            if (weather.main.equals("Rain", ignoreCase = true) ||
                weather.main.equals("Drizzle", ignoreCase = true)) {
                return EstadoClimaType.LLUVIA
            }
        }

        // 3. Si no hay lluvia ni tormenta, usar la primera condición
        return if (weatherList.isNotEmpty()) {
            EstadoClimaType.fromWeatherCondition(weatherList[0].main, weatherList[0].description)
        } else {
            EstadoClimaType.NUBLADO // Por defecto
        }
    }

    /**
     * Extensión para convertir Clima a ClimaResponse
     */
    private fun Clima.toDto() = ClimaResponse(
        timestamp = timestamp,
        temperatura = temperatura,
        humedad = humedad,
        estadoClima = estadoClima
    )
}
