package com.estonianport.clima.scheduler

import com.estonianport.clima.service.ClimaService
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class ClimaScheduler(
    private val climaService: ClimaService
) {

    @Scheduled(cron = "0 0 * * * *") // Cada hora en punto
    fun scheduleWeatherFetch() {
        logger.info { "Ejecutando tarea programada: consulta de clima..." }
        try {
            climaService.fetchAndSaveWeather()
        } catch (e: Exception) {
            logger.error(e) { "Error en tarea programada de clima: ${e.message}" }
        }
    }
}