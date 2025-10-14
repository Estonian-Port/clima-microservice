package com.estonianport.clima.dto

data class OpenWeatherResponse(
    val main: Main,
    val weather: List<Weather>,
) {
    data class Main(
        val temp: Double,
        val humidity: Int
    )

    data class Weather(
        val main: String,
        val description: String
    )
}

