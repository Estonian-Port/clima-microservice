package com.estonianport.clima.service

import com.estonianport.clima.dto.OpenWeatherResponse
import com.estonianport.clima.properties.OpenWeatherConfig
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.time.Duration

private val logger = KotlinLogging.logger {}

@Service
class OpenWeatherService(
    private val webClient: WebClient,
    private val openWeatherConfig: OpenWeatherConfig
) {

    companion object {
        private const val BUENOS_AIRES_LAT = "-34.6037"
        private const val BUENOS_AIRES_LON = "-58.3816"
    }

    fun getCurrentWeather(): OpenWeatherResponse? {
        return try {
            webClient.get()
                .uri { builder ->
                    builder
                        .queryParam("lat", BUENOS_AIRES_LAT)
                        .queryParam("lon", BUENOS_AIRES_LON)
                        .queryParam("appid", openWeatherConfig.apiKey)
                        .queryParam("units", "metric")
                        .queryParam("lang", "es")
                        .build()
                }
                .retrieve()
                .bodyToMono(OpenWeatherResponse::class.java)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume { error ->
                    logger.error(error) { "Error al consultar OpenWeather API: ${error.message}" }
                    Mono.empty()
                }
                .block()
        } catch (e: WebClientResponseException) {
            logger.error(e) { "Error HTTP ${e.statusCode} al consultar OpenWeather: ${e.responseBodyAsString}" }
            null
        } catch (e: Exception) {
            logger.error(e) { "Error inesperado al consultar OpenWeather: ${e.message}" }
            null
        }
    }
}